<h1 align="center">
    <br> 🍲ShareEats🍲
</h1>

![썸네일 테스트](https://user-images.githubusercontent.com/76620764/145583547-cc74c765-eb7d-420a-9dc1-fc3706dc6931.png)

- Wanted Hack.career Hackathon
- 프로젝트 기간: `2021.10.30` ~ `2021.12.10`

<br>
<br>

## Features
- 현위치 또는 선택한 동네를 기준으로 배달 펀딩 게시글 확인 가능
- 선택한 음식점을 기준으로 배달 음식 모집 글 작성
- 작성자와 참여자 간에 채팅 가능
- 참여자가 게시글을 확인하고 메뉴 선택 후 결제를 통해 펀딩 참여
- 최소주문금액을 충족시키면 자동으로 주문 완료
- 주문 완료 직후 펀딩 참여자들에게 주문 완료 푸쉬 알림 전송
- 지난 주문 내역 확인 가능

## Tech
- Third Party
  - Firebase Realtime Database
  - Firebase Storage
  - Firebase Cloud Message
  - NaverMap SDK
  - Naver Cloud API (Geocoding, Reverse Geocoding)
  - KaKao Pay API

- UI
  - ConstraintLayout
  - BottomNavigation
  - TabLayout
  - Fragment
  - AutoCompleteTextView
  - Toolbar
  - Custom ListView

.
.
.


## Problem

📚 LoginFragment.kt
```
private fun initLottie() {
    binding.splash.addAnimatorListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {

        }

        override fun onAnimationEnd(animation: Animator?) {
            if (FourMostPreference.getFirstVisit()) {
                startActivity(Intent(this@SplashActivity, ServiceExplainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, SignInActivity::class.java))
            }
            finish()
        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationRepeat(animation: Animator?) {
        }
    })
}
```
이 코드에서 이런 문제가 있어 이렇게 해결을 블라블라

## Screenshot

1️⃣ 로그인

![와어이프레임_로그인](https://user-images.githubusercontent.com/76620764/145572789-9a08a954-d013-4f92-b4ba-b60773a08f2b.jpg)
<br>

2️⃣ 게시글
![와이어프레임_게시글](https://user-images.githubusercontent.com/76620764/145572911-2acfd1c3-c3f2-486b-a171-e75d40bf4f63.jpg)
<br>

3️⃣ 채팅
![와어이프레임_채팅](https://user-images.githubusercontent.com/76620764/145572935-1a1324c2-c700-4c77-bcf8-155949b0317d.jpg)
<br>

4️⃣ 마이페이지
![와어이프레임_마이페이지](https://user-images.githubusercontent.com/76620764/145572953-9050a6b2-768d-4b51-b494-5d9223e9310a.jpg)
<br>

## Contributor
<table class="tg">
<tbody>
    <tr>
        <td>김민정</td>
        <td>김주환</td>
        <td>나요셉</td>
        <td>백동열</td>
    </tr>
    <tr>
        <td><a href="">@4z7l</a></td>
        <td><a href="https://github.com/juhwankim-dev">@juhwankim-dev</a></td>
        <td><a href="">@sgh002400</a></td>
        <td><a href="">@micro155</a></td>
    </tr>
    <tr>
        <td><img src="" width="300px"/></td>
        <td><img src="https://user-images.githubusercontent.com/76620764/145577637-1cb20f92-d076-4e3f-91d4-9719a1621542.jpg"  width="300px"/></td>
        <td><img src=""  width="300px"/></td>
        <td><img src="https://avatars.githubusercontent.com/u/69238456?s=400&u=849688e4a8675e363dc45a29b8d3e1cb6d468a01&v=4"  width="300px"/></td>
    </tr>
    <tr>
        <td></td>
        <td>로그인, 회원가입, 마이페이지<br>주문내역, 회원정보 수정, 공지사항</td>
        <td></td>
        <td>게시글 작성, 게시글 목록, 게시글 검색<br>참여하기</td>
    </tr>
</tbody>
</table>
