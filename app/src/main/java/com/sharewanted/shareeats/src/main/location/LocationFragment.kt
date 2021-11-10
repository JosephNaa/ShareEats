package com.sharewanted.shareeats.src.main.location

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.sharewanted.shareeats.R
import com.sharewanted.shareeats.databinding.FragmentLocationBinding
import com.sharewanted.shareeats.service.GeocodeService
import com.sharewanted.shareeats.src.main.location.model.MarkerInfo
import com.sharewanted.shareeats.util.RetrofitCallback
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class LocationFragment : Fragment(), OnMapReadyCallback, TextView.OnEditorActionListener {

    private val TAG = "LocationFragment"
    private val PERMISSION_REQUEST_CODE = 100

    private lateinit var binding: FragmentLocationBinding
    private lateinit var mLocationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private lateinit var mapFragment: MapFragment
    private lateinit var search: TextView
    private lateinit var storeMarkers: MutableList<Marker>
    private lateinit var placeMarkers: MutableList<Marker>
    private lateinit var placeInfoList: MutableList<MarkerInfo>
    private lateinit var infoWindow: InfoWindow

    private lateinit var database: FirebaseDatabase
    private lateinit var storeRef: DatabaseReference
    private lateinit var postRef: DatabaseReference
    private lateinit var storeEventListener: ChildEventListener
    private lateinit var postEventListener: ChildEventListener


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentLocationBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance()
        storeRef = database.getReference("Store")
        postRef = database.getReference("Post")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        search = view.findViewById(R.id.editTextTextPersonName)
        search.setOnEditorActionListener(this)

        // 상점 정보 불러오기
//        initDatabase()

        // 게시글의 배달 위치 불러오기
        initPost()

        // 네이버 지도 초기화
        initMap()

        // 현재 위치를 반환하는 구현체
        mLocationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE)
    }

    /* TedPermission 사용하여 위치 권한 취득 */
    private fun checkPermission() {
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                Log.d(TAG, "위치 권한 확인.")

                // 현재 위치 추적
                naverMap.locationTrackingMode = LocationTrackingMode.Follow
                naverMap.setOnMapClickListener { pointF, latLng ->
                    infoWindow.close()
                }
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                Toast.makeText(requireContext(), "위치 권한이 거부되었습니다", Toast.LENGTH_SHORT).show()
            }
        }

        TedPermission.create()
            .setPermissionListener(permissionListener)
            .setDeniedMessage("If you reject permission, you can not use this service\\n\\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .check()
    }

    private fun initDatabase() {
        val executor: Executor = Executors.newFixedThreadPool(2)
        val handler = Handler(Looper.getMainLooper())

        storeEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val location = snapshot.child("location").getValue<String>()!!

                Log.d(TAG, "value = $location")
                val res = GeocodeService().getGeocode(location, getGeocodeCallback())

                res.observe(viewLifecycleOwner, { res ->
                    Log.d(TAG, "livedata = ${res.addresses}")

                    executor.execute {
                        storeMarkers = mutableListOf()

                        // BackgroundThread에서 마커 정보 초기화
                        repeat(1) {
                            for (address in res.addresses) {
                                Log.d(TAG, "도로명주소 = ${address.roadAddress}")
                                storeMarkers += Marker().apply {
                                    position = LatLng(address.y, address.x)
                                    icon = MarkerIcons.YELLOW
                                }
                            }
                        }

                        handler.post {
                            // MainThread에서 지도에 마커 표시
                            storeMarkers.forEach { marker ->
                                run {
                                    marker.map = naverMap
                                }
                            }
                            Log.d(TAG, "store size = ${storeMarkers.size}")
                        }
                    }
                })
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "Child changed.")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "Child moved.")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.d(TAG, "Child deleted.")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "$error")
            }
        }

        storeRef.addChildEventListener(storeEventListener)
    }

    private fun initPost() {
        val executor: Executor = Executors.newFixedThreadPool(2)
        val handler = Handler(Looper.getMainLooper())
        placeMarkers = mutableListOf()
        placeInfoList = mutableListOf()

        postEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val location = snapshot.child("place").getValue<String>()!!
                val title = snapshot.child("title").getValue<String>()!!
                val storeId = snapshot.child("storeId").getValue<String>()!!
                val res = GeocodeService().getGeocode(location, getGeocodeCallback())

                Log.d(TAG, "title = $title, storeId = $storeId")

                var storeName = ""

                storeRef.child(storeId).child("name").get().addOnSuccessListener {
                    storeName = it.getValue<String>()!!
                }

                res.observe(viewLifecycleOwner, { res ->
                    Log.d(TAG, "livedata = ${res.addresses}")

                    executor.execute {

                        infoWindow = InfoWindow()

                        // BackgroundThread에서 마커 정보 초기화
                        repeat(1) {
                            for (address in res.addresses) {
                                Log.d(TAG, "store_value = $storeName")
                                Log.d(TAG, "도로명주소 = ${address.roadAddress}")

                                val marker = Marker()
                                marker.position = LatLng(address.y, address.x)
                                marker.icon = MarkerIcons.RED
                                marker.onClickListener = markerListener
                                marker.tag = "제목: $title \n주문 매장: $storeName"

                                placeMarkers += marker
                                placeInfoList += MarkerInfo(marker, title, storeName)

//                                placeMarkers += Marker().apply {
//                                    position = LatLng(address.y, address.x)
//                                    icon = MarkerIcons.RED
//                                    marker.
//                                }

                            }
                        }

                        handler.post {
                            infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(requireContext()) {
                                override fun getText(infoWindow: InfoWindow): CharSequence {
                                    return infoWindow.marker?.tag as CharSequence ?: ""
                                }
                            }

                            // MainThread에서 지도에 마커 표시
                            placeInfoList.forEach { markerInfo ->
                                run {
                                    markerInfo.marker.map = naverMap
                                    Log.d(TAG, "title = ${markerInfo.title}, store = ${markerInfo.storeName}")
                                    infoWindow.open(markerInfo.marker)
                                }
                            }
                            Log.d(TAG, "place size = ${placeMarkers.size}")
                            Log.d(TAG, "info size = ${placeInfoList.size}")
                        }
                    }
                })
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "Child changed.")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.d(TAG, "Child moved.")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "Child deleted.")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "$error")
            }
        }

        postRef.addChildEventListener(postEventListener)
    }

    private fun initMap() {
        val fm = childFragmentManager
        mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }

        // 비동기로 onMapReady 콜백 함수 호출, onMapReady에서 NaverMap 객체 초기화
        mapFragment.getMapAsync(this)
    }

    @UiThread
    override fun onMapReady(map: NaverMap) {
        Log.d(TAG, "onMapReady")

        // NaverMap 객체를 받아와 객체에 위치 소스 지정
        naverMap = map
        naverMap.locationSource = mLocationSource

        // UI Controls
        val uiSettings = naverMap.uiSettings
        uiSettings.isCompassEnabled = true // 나침반 아이콘
        uiSettings.isScaleBarEnabled = true // 축척바
        uiSettings.isZoomControlEnabled = true // 지도 줌 버튼
        uiSettings.isLocationButtonEnabled = true // 현재위치 버튼

        // 위치 권한 확인
        checkPermission()
    }

    // 검색 기능
    override fun onEditorAction(tv: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (tv!!.id == R.id.editTextTextPersonName && actionId == EditorInfo.IME_ACTION_DONE) {
            Log.d(TAG, binding.editTextTextPersonName.text.toString())

            val lastLocation = mLocationSource.lastLocation!!

            // Issue #1 : 네이버 Geocode API는 키워드 검색 기능이 없음. 도로명주소/지번주소로만 검색 가능.
            GeocodeService().getGeocode(binding.editTextTextPersonName.text.toString(), getGeocodeCallback())
            search.text = ""
        }
        return false
    }

    inner class getGeocodeCallback: RetrofitCallback<Boolean> {
        override fun onError(t: Throwable) {
            Log.d(TAG, "error")
        }

        override fun onFailure(code: Int) {
            Log.d(TAG, "$code")
        }

        override fun onSuccess(code: Int, responseData: Boolean) {
            Log.d(TAG, "success")
        }
    }

    private val markerListener = Overlay.OnClickListener { overlay ->
        val marker = overlay as Marker

        if (marker.infoWindow == null) {
            // 현재 마커에 정보 창이 열려있지 않을 경우 엶
            infoWindow.open(marker)
        } else {
            // 이미 현재 마커에 정보 창이 열려있을 경우 닫음
            infoWindow.close()
        }

        true
    }
}