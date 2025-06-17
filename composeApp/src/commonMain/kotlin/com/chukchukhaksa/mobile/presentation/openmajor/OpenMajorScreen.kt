package com.chukchukhaksa.mobile.presentation.openmajor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.open_major_empty_search_result
import chukchukhaksa.composeapp.generated.resources.open_major_need_login_toast
import chukchukhaksa.composeapp.generated.resources.open_major_screen_search_bar_placeholder
import chukchukhaksa.composeapp.generated.resources.word_confirm
import com.chukchukhaksa.mobile.common.designsystem.component.SuwikiBackground
import com.chukchukhaksa.mobile.common.designsystem.component.appbar.SuwikiAppBarWithTitle
import com.chukchukhaksa.mobile.common.designsystem.component.button.SuwikiContainedLargeButton
import com.chukchukhaksa.mobile.common.designsystem.component.loading.LoadingScreen
import com.chukchukhaksa.mobile.common.designsystem.component.searchbar.SuwikiSearchBar
import com.chukchukhaksa.mobile.common.designsystem.component.tabbar.SuwikiTabBar
import com.chukchukhaksa.mobile.common.designsystem.component.tabbar.TabTitle
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import com.chukchukhaksa.mobile.common.ui.shadow.suwikiShadow
import com.chukchukhaksa.mobile.presentation.openmajor.component.OpenMajorContainer
import com.chukchukhaksa.mobile.presentation.openmajor.model.OpenMajor
import com.chukchukhaksa.mobile.presentation.openmajor.model.OpenMajorTap
import kotlinx.collections.immutable.PersistentList
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi

private val OPEN_MAJOR_PAGE_COUNT = OpenMajorTap.entries.size

@Composable
fun OpenMajorRoute(
    viewModel: OpenMajorViewModel = koinViewModel(),
    popBackStack: () -> Unit,
    popBackStackWithArgument: (String) -> Unit,
    handleException: (Throwable) -> Unit,
    onShowToast: (String) -> Unit,
) {
    val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()

    viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
        when (sideEffect) {
            is OpenMajorSideEffect.HandleException -> handleException(sideEffect.throwable)
            OpenMajorSideEffect.PopBackStack -> popBackStack()
            is OpenMajorSideEffect.PopBackStackWithArgument -> popBackStackWithArgument(sideEffect.argument)
            is OpenMajorSideEffect.ShowNeedLoginToast -> {
                onShowToast(getString(Res.string.open_major_need_login_toast))
            }
        }
    }

    val pagerState = rememberPagerState(pageCount = { OPEN_MAJOR_PAGE_COUNT })

    LaunchedEffect(key1 = uiState.currentPage) {
        pagerState.animateScrollToPage(uiState.currentPage)
    }

    snapshotFlow { pagerState.currentPage }.collectWithLifecycle {
        viewModel.syncPagerState(it)
    }

    val allOpenMajorListState = rememberLazyListState()

    val onReachedBottomAllOpenMajor by remember {
        derivedStateOf {
            allOpenMajorListState.isScrolledToEnd()
        }
    }


    LaunchedEffect(onReachedBottomAllOpenMajor) {
        viewModel.changeBottomShadowVisible(!onReachedBottomAllOpenMajor)
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.initData()
    }

    OpenMajorScreen(
        uiState = uiState,
        pagerState = pagerState,
        allOpenMajorListState = allOpenMajorListState,
        onClickClose = viewModel::popBackStack,
        onClickConfirmButton = viewModel::popBackStackWithArgument,
        onClickOpenMajorContainer = viewModel::updateSelectedOpenMajor,
        onClickTab = viewModel::syncPagerState,
        onValueChangeSearchBar = viewModel::updateSearchValue,
        onClickSearchBarClearButton = { viewModel.updateSearchValue("") },
    )
}

@Composable
fun OpenMajorScreen(
    uiState: OpenMajorState = OpenMajorState(),
    pagerState: PagerState = rememberPagerState(pageCount = { OPEN_MAJOR_PAGE_COUNT }),
    allOpenMajorListState: LazyListState = rememberLazyListState(),
    onClickClose: () -> Unit = {},
    onClickConfirmButton: () -> Unit = {},
    onClickOpenMajorContainer: (String) -> Unit = {},
    onValueChangeSearchBar: (String) -> Unit = {},
    onClickSearchBarClearButton: () -> Unit = {},
    onClickTab: (Int) -> Unit = {},
) {
    SuwikiBackground(
        color = White,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            SuwikiAppBarWithTitle(
                showBackIcon = false,
                title = "개설학과",
                onClickClose = onClickClose,
            )

            SuwikiSearchBar(
                modifier = Modifier.padding(vertical = 10.dp),
                placeholder = stringResource(Res.string.open_major_screen_search_bar_placeholder),
                value = uiState.searchValue,
                onClickClearButton = onClickSearchBarClearButton,
                onValueChange = onValueChangeSearchBar,
            )

            SuwikiTabBar(
                selectedTabPosition = pagerState.currentPage,
            ) {
                OpenMajorTap.entries.forEach { tab ->
                    with(tab) {
                        TabTitle(
                            title = stringResource(titleId),
                            position = position,
                            selected = pagerState.currentPage == position,
                            onClick = { onClickTab(position) },
                        )
                    }
                }
            }

            HorizontalPager(
                modifier = Modifier.weight(1f),
                state = pagerState,
            ) { page ->
                when (OpenMajorTap.entries[page]) {
                    OpenMajorTap.ALL -> {
                        if (uiState.showAllOpenMajorEmptySearchResultScreen) {
                            EmptyText(stringResource(Res.string.open_major_empty_search_result))
                        } else {
                            OpenMajorLazyColumn(
                                listState = allOpenMajorListState,
                                openMajorList = uiState.filteredAllOpenMajorList,
                                onClickOpenMajorContainer = onClickOpenMajorContainer,
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            ) {
                SuwikiContainedLargeButton(
                    modifier = Modifier
                        .imePadding()
                        .suwikiShadow(
                            color = if (uiState.showBottomShadow) White else Color.Transparent,
                            blurRadius = 80.dp,
                            spread = 50.dp,
                        ),
                    text = stringResource(Res.string.word_confirm),
                    onClick = onClickConfirmButton,
                )
            }
        }

        if (uiState.isLoading) LoadingScreen()
    }
}

@Composable
private fun EmptyText(
    text: String = "",
) {
    Text(
        modifier = Modifier
            .padding(52.dp)
            .fillMaxSize(),
        textAlign = TextAlign.Center,
        text = text,
        style = SuwikiTheme.typography.header4,
        color = Gray95,
    )
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun OpenMajorLazyColumn(
    listState: LazyListState,
    openMajorList: PersistentList<OpenMajor>,
    onClickOpenMajorContainer: (String) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(top = 12.dp),
    ) {
        items(
            items = openMajorList,
            key = { it.id },
        ) { openMajor ->
            with(openMajor) {
                OpenMajorContainer(
                    text = name,
                    isChecked = isSelected,
                    onClick = { onClickOpenMajorContainer(name) },
                )
            }
        }
    }
}

//@OptIn(ExperimentalFoundationApi::class)
//@Preview(showBackground = true)
//@Composable
//fun OpenMajorScreenPreview() {
//  SuwikiTheme {
//    OpenMajorScreen()
//  }
//}

fun LazyListState.isScrolledToEnd() =
    layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1

