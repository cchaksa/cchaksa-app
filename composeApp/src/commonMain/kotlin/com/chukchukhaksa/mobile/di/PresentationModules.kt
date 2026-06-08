package com.chukchukhaksa.mobile.di

import com.chukchukhaksa.mobile.MainViewModel
import com.chukchukhaksa.mobile.presentation.timetable.timetable.component.bottomsheet.openmajor.OpenMajorBottomSheetViewModel
import com.chukchukhaksa.mobile.presentation.timetable.celleditor.CellEditorViewModel
import com.chukchukhaksa.mobile.presentation.timetable.openlecture.OpenLectureViewModel
import com.chukchukhaksa.mobile.presentation.landing.LandingViewModel
import com.chukchukhaksa.mobile.presentation.timetable.timetable.HomeViewModel
import com.chukchukhaksa.mobile.presentation.timetable.timetablenameinput.TimetableNameInputViewModel
import com.chukchukhaksa.mobile.presentation.timetable.timetableeditor.TimetableEditorViewModel
import com.chukchukhaksa.mobile.presentation.timetable.timetablelist.TimetableListViewModel
import com.chukchukhaksa.mobile.presentation.timetable.semesterselect.SemesterSelectViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::LandingViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::TimetableListViewModel)
    viewModelOf(::TimetableNameInputViewModel)
    viewModelOf(::TimetableEditorViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::CellEditorViewModel)
    viewModelOf(::OpenLectureViewModel)
    viewModelOf(::OpenMajorBottomSheetViewModel)
    viewModelOf(::SemesterSelectViewModel)
}
