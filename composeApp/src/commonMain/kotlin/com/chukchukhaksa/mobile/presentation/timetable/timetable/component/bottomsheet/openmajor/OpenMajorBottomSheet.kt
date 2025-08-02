package com.chukchukhaksa.mobile.presentation.timetable.timetable.component.bottomsheet.openmajor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chukchukhaksa.composeapp.generated.resources.Res
import chukchukhaksa.composeapp.generated.resources.open_major_empty_search_result
import chukchukhaksa.composeapp.generated.resources.open_major_screen_search_bar_placeholder
import com.chukchukhaksa.mobile.common.designsystem.component.bottomsheet.CchBottomSheet
import com.chukchukhaksa.mobile.common.designsystem.component.loading.LoadingScreen
import com.chukchukhaksa.mobile.common.designsystem.component.textfield.CchSearchTextField
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray95
import com.chukchukhaksa.mobile.common.designsystem.theme.SuwikiTheme
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.bottomsheet.openmajor.model.OpenMajor
import kotlinx.collections.immutable.PersistentList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenMajorBottomSheet(
  selectedOpenMajor: String?,
  viewModel: OpenMajorBottomSheetViewModel = koinViewModel(),
  onDismissRequest: () -> Unit,
  onConfirm: (String?) -> Unit,
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
      onClickOpenMajorContainer = viewModel::updateSelectedOpenMajor,
      onValueChangeSearchBar = viewModel::updateSearchValue,
    )
  }
}

@Composable
private fun OpenMajorBottomSheetContent(
  uiState: OpenMajorState = OpenMajorState(),
  allOpenMajorListState: LazyListState = rememberLazyListState(),
  onClickOpenMajorContainer: (String?) -> Unit = {},
  onValueChangeSearchBar: (String) -> Unit = {},
) {
  Column {
    CchSearchTextField(
      modifier = Modifier.padding(horizontal = 20.dp),
      value = uiState.searchValue,
      onValueChange = onValueChangeSearchBar,
      placeholder = stringResource(Res.string.open_major_screen_search_bar_placeholder),
      onSearchAction = {},
    )

    Spacer(Modifier.height(8.dp))

    if (uiState.showAllOpenMajorEmptySearchResultScreen) {
      EmptyText(stringResource(Res.string.open_major_empty_search_result))
    } else {
      OpenMajorLazyColumn(
        listState = allOpenMajorListState,
        openMajorList = uiState.filteredAllOpenMajorList,
        searchValue = uiState.searchValue,
        onClickOpenMajorContainer = onClickOpenMajorContainer,
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
  searchValue: String = "",
  onClickOpenMajorContainer: (String?) -> Unit = {},
) {
  LazyColumn(
    modifier = modifier.height(56.dp * 5),
    state = listState,
  ) {
    items(
      items = openMajorList,
      key = { it.id },
    ) { openMajor ->
      with(openMajor) {
        OpenMajorItem(
          text = name ?: "전체",
          searchValue = searchValue,
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

