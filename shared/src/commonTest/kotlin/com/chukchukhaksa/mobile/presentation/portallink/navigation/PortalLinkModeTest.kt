package com.chukchukhaksa.mobile.presentation.portallink.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PortalLinkModeTest {

  @Test
  fun `portal login urls on both hosts become link mode`() {
    assertEquals(PortalLinkMode.LINK, PortalLinkMode.fromWebUrl("https://dv.cchaksa.com/mpa/portal-login"))
    assertEquals(PortalLinkMode.LINK, PortalLinkMode.fromWebUrl("https://www.cchaksa.com/mpa/portal-login"))
  }

  @Test
  fun `resync login url becomes resync mode`() {
    assertEquals(PortalLinkMode.RESYNC, PortalLinkMode.fromWebUrl("https://dv.cchaksa.com/mpa/resync/login"))
  }

  @Test
  fun `query fragment and trailing slash are ignored`() {
    assertEquals(PortalLinkMode.RESYNC, PortalLinkMode.fromWebUrl("https://www.cchaksa.com/mpa/resync/login?from=home#top"))
    assertEquals(PortalLinkMode.LINK, PortalLinkMode.fromWebUrl("https://www.cchaksa.com/mpa/portal-login/"))
  }

  @Test
  fun `other web pages stay in web view`() {
    assertNull(PortalLinkMode.fromWebUrl("https://www.cchaksa.com/mpa/graduation-progress"))
    assertNull(PortalLinkMode.fromWebUrl("https://www.cchaksa.com/mpa/resync/scraping"))
    assertNull(PortalLinkMode.fromWebUrl("https://www.cchaksa.com/"))
    assertNull(PortalLinkMode.fromWebUrl("https://www.cchaksa.com/mpa/portal-login/scraping"))
  }

  @Test
  fun `same path on another host is not intercepted`() {
    assertNull(PortalLinkMode.fromWebUrl("https://evil.example.com/mpa/portal-login"))
    assertNull(PortalLinkMode.fromWebUrl("https://notcchaksa.com/mpa/portal-login"))
    assertNull(PortalLinkMode.fromWebUrl("/mpa/portal-login"))
  }
}
