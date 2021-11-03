package com.sharewanted.shareeats.src.main.mypage.orderdetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sharewanted.shareeats.R

class OrderDetailAdapter(var list: MutableList<OrderDetail>) : RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder>() {

    inner class OrderDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fragment_my_page_order_detail_list_item_ivStore = itemView.findViewById<ImageView>(R.id.fragment_my_page_order_detail_list_item_ivStore)
        var fragment_my_page_order_detail_list_item_tvTitle = itemView.findViewById<TextView>(R.id.fragment_my_page_creator_list_item_tvTitle)
        var fragment_my_page_order_detail_list_item_tvStoreName = itemView.findViewById<TextView>(R.id.fragment_my_page_creator_list_item_tvEmail)
        var fragment_my_page_order_detail_list_item_tvDate = itemView.findViewById<TextView>(R.id.fragment_my_page_order_detail_list_item_tvDate)
        var fragment_my_page_order_detail_list_item_price = itemView.findViewById<TextView>(R.id.fragment_my_page_order_detail_list_item_price)

        fun onBind(o: OrderDetail) {
            fragment_my_page_order_detail_list_item_ivStore.setImageDrawable(o.storeImg)
            fragment_my_page_order_detail_list_item_tvTitle.text = o.title
            fragment_my_page_order_detail_list_item_tvStoreName.text = o.storeName
            fragment_my_page_order_detail_list_item_tvDate.text = o.date.toString() // 수정해야함 지금은 귀찮
            fragment_my_page_order_detail_list_item_price.text = o.price
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_my_page_list_item_order_detail, parent, false)
        return OrderDetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderDetailAdapter.OrderDetailViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}