package com.chukchukhaksa.mobile.data.portallink.model

/** 포털 연동에 쓰는 경로. 출시 빌드는 늘 [DEVICE_FIRST]이고, 디버그 빌드는 디버그 화면에서 바꿀 수 있다. */
enum class PortalLinkStrategy {
  /** 기기에서 스크래핑해 올리고, 기기 쪽 문제로 실패하면 서버 job으로 넘어간다. */
  DEVICE_FIRST,

  /** 기기에서 스크래핑해 올리기만 한다. 실패해도 서버 job으로 넘어가지 않는다. */
  DEVICE_ONLY,

  /** 기기에서 스크래핑하지 않고 바로 서버 job을 만든다. */
  SERVER_ONLY,
}
