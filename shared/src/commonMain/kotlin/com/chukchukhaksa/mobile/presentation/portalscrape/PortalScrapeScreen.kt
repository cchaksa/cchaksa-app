package com.chukchukhaksa.mobile.presentation.portalscrape

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chukchukhaksa.mobile.common.designsystem.component.SuwikiBackground
import com.chukchukhaksa.mobile.common.designsystem.component.appbar.CchAppBarWithTitle
import com.chukchukhaksa.mobile.common.designsystem.component.button.CchBasicButton
import com.chukchukhaksa.mobile.common.designsystem.component.textfield.CchRegularTextField
import com.chukchukhaksa.mobile.common.designsystem.theme.Black100
import com.chukchukhaksa.mobile.common.designsystem.theme.CchTheme
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray100
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray400
import com.chukchukhaksa.mobile.common.designsystem.theme.Gray500
import com.chukchukhaksa.mobile.common.designsystem.theme.Purple600
import com.chukchukhaksa.mobile.common.designsystem.theme.Red100
import com.chukchukhaksa.mobile.common.designsystem.theme.Red400
import com.chukchukhaksa.mobile.common.designsystem.theme.White
import com.chukchukhaksa.mobile.common.designsystem.theme.White100
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeResult
import com.chukchukhaksa.mobile.common.model.portalscrape.PortalScrapeStep
import com.chukchukhaksa.mobile.common.ui.cchClickable
import com.chukchukhaksa.mobile.common.ui.collectWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PortalScrapeRoute(
  viewModel: PortalScrapeViewModel = koinViewModel(),
  popBackStack: () -> Unit,
  onShowToast: (String, Dp) -> Unit,
) {
  val uiState by viewModel.mviStore.uiState.collectAsStateWithLifecycle()
  val clipboardManager = LocalClipboardManager.current

  viewModel.mviStore.sideEffects.collectWithLifecycle { sideEffect ->
    when (sideEffect) {
      PortalScrapeSideEffect.PopBackStack -> popBackStack()
      is PortalScrapeSideEffect.CopyRawJson -> {
        clipboardManager.setText(AnnotatedString(sideEffect.json))
        onShowToast("원문 JSON을 복사했습니다", 70.dp)
      }
    }
  }

  PortalScrapeScreen(
    uiState = uiState,
    onStudentIdChange = viewModel::updateStudentId,
    onPasswordChange = viewModel::updatePassword,
    onTogglePasswordVisible = viewModel::togglePasswordVisible,
    onClickStart = viewModel::startScrape,
    onClickCopyRawJson = viewModel::copyRawJson,
    onClickBack = viewModel::popBackStack,
  )
}

/**
 * suwon-scraper-v1을 기기 안으로 옮긴 스크래핑을 실제 계정으로 실행해 보는 디버그 화면.
 * 디자인 시스템 화면이 아니라 검증 도구이므로 색상·타이포 토큰만 재사용한다.
 */
@Composable
fun PortalScrapeScreen(
  uiState: PortalScrapeState = PortalScrapeState(),
  onStudentIdChange: (String) -> Unit = {},
  onPasswordChange: (String) -> Unit = {},
  onTogglePasswordVisible: () -> Unit = {},
  onClickStart: () -> Unit = {},
  onClickCopyRawJson: () -> Unit = {},
  onClickBack: () -> Unit = {},
) {
  SuwikiBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(White),
    ) {
      CchAppBarWithTitle(
        title = "포털 스크래핑 테스트",
        onClickBackButton = onClickBack,
      )

      Column(
        modifier = Modifier
          .fillMaxSize()
          .imePadding()
          .verticalScroll(rememberScrollState())
          .padding(start = 20.dp, end = 20.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = "suwon-scraper-v1과 같은 순서(포털 로그인 → 학사 SSO → 조회 API → 학기별 병합)를 이 기기에서 직접 실행합니다. " +
            "입력한 계정 정보는 저장하지 않습니다.",
          style = CchTheme.typography.bodySm,
          color = Gray500,
        )

        CchRegularTextField(
          value = uiState.studentId,
          placeholder = "학번",
          isActive = uiState.studentId.isNotEmpty(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
          onValueChanged = onStudentIdChange,
        )

        CchRegularTextField(
          value = uiState.password,
          placeholder = "비밀번호",
          isActive = uiState.password.isNotEmpty(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
          visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          onValueChanged = onPasswordChange,
        )

        Text(
          modifier = Modifier
            .align(Alignment.End)
            .clip(RoundedCornerShape(6.dp))
            .cchClickable(onClick = onTogglePasswordVisible)
            .padding(horizontal = 4.dp, vertical = 2.dp),
          text = if (uiState.isPasswordVisible) "비밀번호 숨기기" else "비밀번호 보기",
          style = CchTheme.typography.bodySm,
          color = Gray500,
        )

        CchBasicButton(
          modifier = Modifier.fillMaxWidth(),
          text = if (uiState.isRunning) "실행 중..." else "스크래핑 실행",
          enable = uiState.canStart,
          onClick = onClickStart,
        )

        Section(title = "진행 단계") {
          uiState.steps.forEach { StepRow(it) }
        }

        uiState.error?.let { ErrorCard(it) }

        uiState.result?.let { ResultSections(result = it, onClickCopyRawJson = onClickCopyRawJson) }
      }
    }
  }
}

@Composable
private fun Section(
  title: String,
  content: @Composable ColumnScope.() -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(text = title, style = CchTheme.typography.bodyMdStrong, color = Black100)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(Gray100)
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      content = content,
    )
  }
}

@Composable
private fun StepRow(state: PortalScrapeStepState) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(modifier = Modifier.size(16.dp), contentAlignment = Alignment.Center) {
      if (state.status == PortalScrapeStepStatus.RUNNING) {
        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Purple600, strokeWidth = 2.dp)
      } else {
        Text(text = state.status.marker, style = CchTheme.typography.bodySmStrong, color = state.status.color)
      }
    }
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      modifier = Modifier.weight(1f),
      text = state.step.label,
      style = CchTheme.typography.bodyMd,
      color = Black100,
    )
    Text(
      text = state.statusText(),
      style = CchTheme.typography.bodySm,
      color = state.status.color,
      textAlign = TextAlign.End,
    )
  }
}

@Composable
private fun ErrorCard(error: PortalScrapeErrorState) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(Red100)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Text(
      text = "실패 · ${error.code}${if (error.retryable) " (재시도 가능)" else ""}",
      style = CchTheme.typography.bodyMdStrong,
      color = Red400,
    )
    SelectionContainer {
      Text(text = error.message, style = CchTheme.typography.bodySm, color = Black100)
    }
  }
}

@Composable
private fun ResultSections(
  result: PortalScrapeResult,
  onClickCopyRawJson: () -> Unit,
) {
  val student = result.student
  Section(title = "학생 정보") {
    InfoRow("이름", "${student.name.orDash()} (${student.studentNumber.orDash()})")
    InfoRow("소속", listOfNotNull(student.college, student.department, student.major).joinToString(" · ").ifEmpty { "-" })
    InfoRow("학적", "${student.status.orDash()} · ${student.grade?.let { "${it}학년" } ?: "-"} · 이수 ${student.completedSemesterCount}학기")
    InfoRow(
      "입학",
      "${student.admissionYear.orDash()}년 ${semesterName(student.admissionSemesterCode)} · ${if (student.isTransfer) "편입생" else "신입생"}",
    )
    InfoRow("외국어 인증", student.foreignLanguageStatus.orDash())
  }

  result.cumulativeRecord?.let { record ->
    Section(title = "누적 성적") {
      InfoRow("평점평균", record.gpa.orDash())
      InfoRow("취득 / 신청 학점", "${record.earnedCredits.orDash()} / ${record.attemptedCredits.orDash()}")
      InfoRow("백분위", record.percentile.orDash())
    }
  }

  Section(title = "학기별 수강·성적 (${result.semesters.size}개 학기 · ${result.semesters.sumOf { it.courseCount }}과목)") {
    if (result.semesters.isEmpty()) {
      Text(text = "조회된 학기가 없습니다.", style = CchTheme.typography.bodySm, color = Gray500)
    }
    result.semesters.forEach { semester ->
      InfoRow(
        "${semester.year}년 ${semesterName(semester.semesterCode)}",
        "${semester.courseCount}과목 · ${semester.totalPoint.formatPoint()}학점",
      )
    }
  }

  Section(title = "편입생 지정과목") {
    InfoRow("지정과목", if (student.isTransfer) "${result.designatedCourseCount}건" else "해당 없음 (편입생 아님)")
  }

  if (result.warnings.isNotEmpty()) {
    Section(title = "주의") {
      result.warnings.forEach { Text(text = it, style = CchTheme.typography.bodySm, color = Red400) }
    }
  }

  Section(title = "원문 JSON") {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        modifier = Modifier.weight(1f),
        text = "${result.rawJson.length.formatLength()} · 총 ${result.elapsedMillis.formatElapsed()} 소요",
        style = CchTheme.typography.bodySm,
        color = Gray500,
      )
      Text(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(Black100)
          .cchClickable(onClick = onClickCopyRawJson)
          .padding(horizontal = 12.dp, vertical = 6.dp),
        text = "복사",
        style = CchTheme.typography.bodySmStrong,
        color = White100,
      )
    }
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 320.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(White100)
        .verticalScroll(rememberScrollState())
        .padding(10.dp),
    ) {
      SelectionContainer {
        Text(
          text = result.rawJson.take(RAW_JSON_PREVIEW_LENGTH) +
            if (result.rawJson.length > RAW_JSON_PREVIEW_LENGTH) "\n... (전체 내용은 복사해서 확인하세요)" else "",
          style = CchTheme.typography.bodyXs,
          fontFamily = FontFamily.Monospace,
          color = Black100,
        )
      }
    }
  }
}

@Composable
private fun InfoRow(label: String, value: String) {
  Row {
    Text(
      modifier = Modifier.width(96.dp),
      text = label,
      style = CchTheme.typography.bodySm,
      color = Gray500,
    )
    SelectionContainer(modifier = Modifier.weight(1f)) {
      Text(text = value, style = CchTheme.typography.bodySm, color = Black100)
    }
  }
}

private const val RAW_JSON_PREVIEW_LENGTH = 4_000

private val PortalScrapeStep.label: String
  get() = when (this) {
    PortalScrapeStep.PORTAL_LOGIN -> "포털 로그인"
    PortalScrapeStep.ACADEMIC_SSO -> "학사 시스템 연결(SSO)"
    PortalScrapeStep.STUDENT -> "학생 정보"
    PortalScrapeStep.COURSES -> "수강 내역"
    PortalScrapeStep.CREDITS -> "성적"
    PortalScrapeStep.DESIGNATED_COURSES -> "편입생 지정과목"
    PortalScrapeStep.MERGE -> "학기별 병합"
  }

private val PortalScrapeStepStatus.marker: String
  get() = when (this) {
    PortalScrapeStepStatus.PENDING, PortalScrapeStepStatus.RUNNING -> "·"
    PortalScrapeStepStatus.DONE -> "✓"
    PortalScrapeStepStatus.SKIPPED -> "-"
    PortalScrapeStepStatus.FAILED -> "✕"
    PortalScrapeStepStatus.INTERRUPTED -> "!"
  }

private val PortalScrapeStepStatus.color: Color
  get() = when (this) {
    PortalScrapeStepStatus.DONE, PortalScrapeStepStatus.RUNNING -> Purple600
    PortalScrapeStepStatus.FAILED -> Red400
    PortalScrapeStepStatus.PENDING, PortalScrapeStepStatus.SKIPPED, PortalScrapeStepStatus.INTERRUPTED -> Gray400
  }

private fun PortalScrapeStepState.statusText(): String = when (status) {
  PortalScrapeStepStatus.PENDING -> "대기"
  PortalScrapeStepStatus.RUNNING -> "진행 중"
  PortalScrapeStepStatus.DONE -> listOfNotNull(detail, elapsedMillis?.formatElapsed()).joinToString(" · ")
  PortalScrapeStepStatus.SKIPPED -> "건너뜀${detail?.let { " ($it)" }.orEmpty()}"
  PortalScrapeStepStatus.FAILED -> "실패${elapsedMillis?.let { " · ${it.formatElapsed()}" }.orEmpty()}"
  PortalScrapeStepStatus.INTERRUPTED -> "중단됨"
}

/** 포털 학기 코드: 10 1학기, 15 여름학기, 20 2학기, 25 겨울학기. */
private fun semesterName(code: String?): String = when (code) {
  "10" -> "1학기"
  "15" -> "여름학기"
  "20" -> "2학기"
  "25" -> "겨울학기"
  null -> "-"
  else -> "${code}학기"
}

private fun String?.orDash(): String = if (isNullOrBlank()) "-" else this

private fun Long.formatElapsed(): String =
  if (this < 1_000) "${this}ms" else "${this / 1_000}.${(this % 1_000) / 100}초"

private fun Double.formatPoint(): String =
  if (this % 1.0 == 0.0) toLong().toString() else ((this * 10).toLong() / 10.0).toString()

private fun Int.formatLength(): String =
  if (this < 1_024) "${this}자" else "${this / 1_024}.${(this % 1_024) * 10 / 1_024}K자"
