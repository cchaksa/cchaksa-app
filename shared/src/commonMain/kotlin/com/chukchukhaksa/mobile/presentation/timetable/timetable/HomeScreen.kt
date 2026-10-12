package com.chukchukhaksa.mobile.presentation.timetable.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.timetable_screen_need_create_timetable
import chukchukhaksa.composeapp.generated.resources.timetable_screen_select_type_cell_title
import com.chukchukhaksa.mobile.common.designsystem.component.SuwikiBackground
import com.chukchukhaksa.mobile.common.designsystem.component.bottomsheet.CchSelectBottomSheet
import com.chukchukhaksa.mobile.common.designsystem.component.dialog.CchDialog
import com.chukchukhaksa.mobile.common.model.TimetableCell
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import com.chukchukhaksa.mobile.presentation.timetable.navigation.argument.CellEditorArgument
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.EditTimetableCellBottomSheet
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.IdfaDebugDialog
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.TimetableAppbar
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.TimetableEmptyColumn
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.Timetable
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.timetable.cell.TimetableCellType
import com.chukchukhaksa.mobile.common.designsystem.component.tabbar.HomeTabBar
import com.chukchukhaksa.mobile.common.designsystem.component.webview.webMyPageUrl
import com.chukchukhaksa.mobile.common.designsystem.component.webview.webPortalLoginUrl
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.chukchukhaksa.mobile.widget.sendWidgetUpdateCommand
import com.chukchukhaksa.mobile.common.provider.LocalAppContext
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.WebViewGuideScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun HomeRoute(
  padding: PaddingValues,
  viewModel: HomeViewModel = koinViewModel(),
  navigateOpenLecture: () -> Unit,
  navigateTimetableList: () -> Unit,
  handleException: (Throwable) -> Unit,
  onShowToast: (String, Dp) -> Unit,
  navigateCellEditor: (CellEditorArgument) -> Unit,
  navigateSemesterSelect: () -> Unit,
  navigateWebView: (String) -> Unit,
  navigateToLanding: () -> Unit,
) {
  val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()

  val context = LocalAppContext.current
  val clipboardManager = LocalClipboardManager.current
  viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
    when (sideEffect) {
      is HomeSideEffect.HandleException -> handleException(sideEffect.throwable)
      HomeSideEffect.NavigateAddTimetableCell -> navigateOpenLecture()
      HomeSideEffect.ShowNeedCreateTimetableToast -> onShowToast(getString(Res.string.timetable_screen_need_create_timetable), 70.dp)
      is HomeSideEffect.NavigateCellEditor -> navigateCellEditor(sideEffect.argument)
      HomeSideEffect.NavigateTimetableList -> navigateTimetableList()
      HomeSideEffect.NavigateSemesterSelect -> navigateSemesterSelect()
    }
  }

  LaunchedEffect(key1 = Unit) {
    viewModel.getMainTimetable()
  }

  LaunchedEffect(key1 = uiState.timetable) {
    if (context != null) {
      if (uiState.timetable != null) {
        sendWidgetUpdateCommand(context)
      }
    }
  }

  HomeScreen(
    padding = padding,
    uiState = uiState,
    onClickAddTimetable = viewModel::navigateSemesterSelect,
    onClickAppbarAdd = viewModel::navigateAddTimetableCell,
    onClickTimetableCell = viewModel::showEditCellBottomSheet,
    onDismissEditCellBottomSheet = viewModel::hideEditCellBottomSheet,
    onClickTimetableCellDeleteButton = viewModel::deleteCell,
    onClickTimetableCellEditButton = { cell ->
      viewModel.hideEditCellBottomSheet()
      viewModel.navigateCellEdit(cell)
    },
    onDismissSelectBottomSheet = viewModel::hideSelectCellTypeBottomSheet,
    onClickSelectBottomSheetItem = { position ->
      viewModel.updateCellType(position)
      viewModel.hideSelectCellTypeBottomSheet()
    },
    onClickSetting = viewModel::showSelectCellTypeBottomSheet,
    onClickHamburger = viewModel::navigateTimetableList,
    onClickHome = {
      if (!uiState.isPortalLinked) {
        viewModel.showPortalLinkDialog()
      } else {
        viewModel.showHomeScreen()
      }
    },
    onClickTimetable = viewModel::onClickTimetableTab,
    onClickMyPage = { navigateWebView(webMyPageUrl) },
    onClickWebView = { navigateWebView("https://www.cchaksa.com/") },
    onDismissPortalLinkDialog = viewModel::hidePortalLinkDialog,
    onConfirmPortalLinkDialog = {
      viewModel.hidePortalLinkDialog()
      navigateWebView(webPortalLoginUrl)
    },
    navigateToLanding = navigateToLanding,
    navigateWebView = navigateWebView,
    // 광고 게이트 실패 토스트용. 홈 탭 기본 bottomPadding(70.dp)으로 전역 토스트를 재사용한다.
    onShowToast = { onShowToast(it, 70.dp) },
  )

  // 디버그 전용: 시간표 탭 3연타 제스처로 노출되는 IDFA 진단 다이얼로그.
  uiState.idfaDebugInfo?.let { info ->
    IdfaDebugDialog(
      info = info,
      onClickCopy = {
        // 성공 시 순수 IDFA, 실패 시 값(0값 등)+원인을 함께 담아 공유·추적에 쓰도록 한다.
        val copyText = when {
          info.isValid -> info.id.orEmpty()
          info.id != null -> "${info.id}\n${info.diagnostics}"
          else -> info.diagnostics
        }
        clipboardManager.setText(AnnotatedString(copyText))
        onShowToast(
          if (info.isValid) "IDFA가 복사되었습니다" else "IDFA 진단 정보가 복사되었습니다",
          70.dp,
        )
      },
      onDismiss = viewModel::hideIdfaDebugDialog,
    )
  }
}

@Composable
fun HomeScreen(
  padding: PaddingValues,
  uiState: HomeState = HomeState(),
  onClickAddTimetable: () -> Unit = {},
  onClickAppbarAdd: () -> Unit = {},
  onClickTimetableCell: (TimetableCell) -> Unit = {},
  onDismissEditCellBottomSheet: () -> Unit = {},
  onClickTimetableCellDeleteButton: (TimetableCell) -> Unit = {},
  onClickTimetableCellEditButton: (TimetableCell) -> Unit = {},
  onDismissSelectBottomSheet: () -> Unit = {},
  onClickSelectBottomSheetItem: (Int) -> Unit = {},
  onClickSetting: () -> Unit = {},
  onClickHamburger: () -> Unit = {},
  onClickHome: () -> Unit = {},
  onClickTimetable: () -> Unit = {},
  onClickMyPage: () -> Unit = {},
  onClickWebView: () -> Unit = {},
  navigateToLanding: () -> Unit = {},
  navigateWebView: (String) -> Unit = {},
  onShowToast: (String) -> Unit = {},
  onDismissPortalLinkDialog: () -> Unit = {},
  onConfirmPortalLinkDialog: () -> Unit = {},
) {
    val semester = "${uiState.timetable?.year}년 ${uiState.timetable?.semester}학기"
    SuwikiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            HomeTabBar(
                // 초기 로드 중(selectedTab == null)에는 어떤 탭도 강조하지 않는다.
                selectedTab = uiState.selectedTab,
                onClickHome = { onClickHome() },
                onClickTimetable = { onClickTimetable() },
                onClickMyPage = { onClickMyPage() }
            )

            if(uiState.selectedTab == HomeTab.EMPTY_TIMETABLE) {
              TimetableEmptyColumn(
                modifier = Modifier
                  .fillMaxSize()
                  .background(White100),
                onClickAdd = onClickAddTimetable,
              )
            }

            if(uiState.selectedTab == HomeTab.TIMETABLE) {
              Column {
                TimetableAppbar(
                  modifier = Modifier.padding(top = 8.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
                  semester = semester,
                  name = uiState.timetable?.name,
                  onClickAdd = onClickAppbarAdd,
                  onClickHamburger = onClickHamburger,
                  onClickSetting = onClickSetting,
                )
                Timetable(
                  timetable = uiState.timetable ?: com.chukchukhaksa.mobile.common.model.Timetable(),
                  type = uiState.cellType,
                  onClickTimetableCell = onClickTimetableCell,
                )
              }
            }

            if(uiState.selectedTab == HomeTab.HOME) {
              WebViewGuideScreen(
                navigateToLogin = navigateToLanding,
                navigateWebView = navigateWebView,
                onShowToast = onShowToast,
              )
            }
        }
    }

  if (uiState.showSelectCellTypeBottomSheet) {
    CchSelectBottomSheet(
      onDismissRequest = onDismissSelectBottomSheet,
      onClickItem = onClickSelectBottomSheetItem,
      itemList = TimetableCellType.entries.map { it.text }
        .toPersistentList(),
      title = stringResource(Res.string.timetable_screen_select_type_cell_title),
      selectedPosition = uiState.cellType.ordinal,
    )
  }


  if (uiState.showEditCellBottomSheet) {
    EditTimetableCellBottomSheet(
      onDismissRequest = onDismissEditCellBottomSheet,
      cell = uiState.selectedCell,
      onClickDeleteButton = onClickTimetableCellDeleteButton,
      onClickEditButton = onClickTimetableCellEditButton,
    )
  }

  if (uiState.showPortalLinkDialog) {
    CchDialog(
      headerText = "알림",
      bodyText = "학교 연동 후 이용 가능한 기능입니다.\n학교 연동을 진행하시겠어요?",
      confirmButtonText = "네",
      dismissButtonText = "아니오",
      onDismissRequest = onDismissPortalLinkDialog,
      onClickConfirm = onConfirmPortalLinkDialog,
      onClickDismiss = onDismissPortalLinkDialog,
    )
  }
}

@Preview
@Composable
fun HomeScreenPreview() {
  CchTheme {
      HomeScreen(padding = PaddingValues(0.dp), uiState = HomeState(selectedTab = HomeTab.HOME))
  }
}
