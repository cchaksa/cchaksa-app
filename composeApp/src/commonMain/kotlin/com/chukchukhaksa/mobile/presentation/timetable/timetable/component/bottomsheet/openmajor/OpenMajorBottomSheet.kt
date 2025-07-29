package com.chukchukhaksa.mobile.presentation.timetable.timetable.component.bottomsheet.openmajor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.open_major_empty_search_result
import chukchukhaksa.composeapp.generated.resources.open_major_screen_search_bar_placeholder
import chukchukhaksa.composeapp.generated.resources.word_confirm
import com.chukchukhaksa.mobile.common.designsystem.component.bottomsheet.CchBottomSheet
import com.chukchukhaksa.mobile.common.designsystem.component.button.SuwikiContainedLargeButton
import com.chukchukhaksa.mobile.common.designsystem.component.loading.LoadingScreen
import com.chukchukhaksa.mobile.common.designsystem.component.searchbar.SuwikiSearchBar
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import com.chukchukhaksa.mobile.common.ui.shadow.suwikiShadow
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.bottomsheet.openmajor.model.OpenMajor
import kotlinx.collections.immutable.PersistentList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenMajorBottomSheet(
  selectedOpenMajor: String,
  viewModel: OpenMajorBottomSheetViewModel = koinViewModel(),
  onDismissRequest: () -> Unit,
  onConfirm: (String) -> Unit,
  handleException: (Throwable) -> Unit,
  onShowToast: (String) -> Unit,
) {
    val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()

    viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
        when (sideEffect) {
            is OpenMajorSideEffect.HandleException -> handleException(sideEffect.throwable)
            OpenMajorSideEffect.PopBackStack -> onDismissRequest()
            is OpenMajorSideEffect.PopBackStackWithArgument -> onConfirm(sideEffect.argument)
        }
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

    LaunchedEffect(selectedOpenMajor) {
        viewModel.setInitialSelectedOpenMajor(selectedOpenMajor)
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.initData()
    }

    CchBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        OpenMajorBottomSheetContent(
            uiState = uiState,
            allOpenMajorListState = allOpenMajorListState,
            onClickConfirmButton = viewModel::popBackStackWithArgument,
            onClickOpenMajorContainer = viewModel::updateSelectedOpenMajor,
            onValueChangeSearchBar = viewModel::updateSearchValue,
            onClickSearchBarClearButton = { viewModel.updateSearchValue("") },
        )
    }
}

@Composable
private fun OpenMajorBottomSheetContent(
  uiState: OpenMajorState = OpenMajorState(),
  allOpenMajorListState: LazyListState = rememberLazyListState(),
  onClickConfirmButton: () -> Unit = {},
  onClickOpenMajorContainer: (String) -> Unit = {},
  onValueChangeSearchBar: (String) -> Unit = {},
  onClickSearchBarClearButton: () -> Unit = {},
) {
    Column(
        modifier = Modifier.height(400.dp),
    ) {
        SuwikiSearchBar(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
            placeholder = stringResource(Res.string.open_major_screen_search_bar_placeholder),
            value = uiState.searchValue,
            onClickClearButton = onClickSearchBarClearButton,
            onValueChange = onValueChangeSearchBar,
        )

        if (uiState.showAllOpenMajorEmptySearchResultScreen) {
            EmptyText(stringResource(Res.string.open_major_empty_search_result))
        } else {
            OpenMajorLazyColumn(
                modifier = Modifier.weight(1f),
                listState = allOpenMajorListState,
                openMajorList = uiState.filteredAllOpenMajorList,
                onClickOpenMajorContainer = onClickOpenMajorContainer,
            )
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

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            LoadingScreen()
        }
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
    modifier: Modifier = Modifier,
    listState: LazyListState,
    openMajorList: PersistentList<OpenMajor>,
    onClickOpenMajorContainer: (String) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(top = 12.dp),
    ) {
        items(
            items = openMajorList,
            key = { it.id },
        ) { openMajor ->
            with(openMajor) {
                OpenMajorItem(
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

