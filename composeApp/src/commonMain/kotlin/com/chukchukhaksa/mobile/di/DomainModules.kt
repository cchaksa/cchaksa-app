package com.chukchukhaksa.mobile.di

import com.chukchukhaksa.mobile.domain.config.usecase.CheckNeedForceUpdateUseCase
import com.chukchukhaksa.mobile.domain.openmajor.usecase.GetOpenMajorListUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.DeleteTimetableCellUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.DeleteTimetableUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.GetAllTimetableUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.GetMainTimetableUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.GetOpenLectureListUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.GetTimetableCellTypeUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.InsertTimetableCellUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.InsertTimetableUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.SetMainTimetableCreateTime
import com.chukchukhaksa.mobile.domain.timetable.usecase.SetTimetableCellTypeUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.UpdateOpenLectureIfNeedUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.UpdateTimetableCellUseCase
import com.chukchukhaksa.mobile.domain.timetable.usecase.UpdateTimetableUseCase
import org.koin.dsl.module

val domainModule = module {
    /*TODO("factory / single 생성 방식 검토 필요")*/
    /*TODO("UseCase(인자 추가 되면 get() 함수 추가)")*/

    // OpenMajor
    factory { GetOpenMajorListUseCase() }

    // Timetable management use cases
    factory { DeleteTimetableUseCase(get()) }
    factory { GetAllTimetableUseCase(get()) }
    factory { GetMainTimetableUseCase(get()) }
    factory { InsertTimetableUseCase(get()) }
    factory { UpdateTimetableUseCase(get()) }
    factory { SetMainTimetableCreateTime(get()) }

    // Timetable cell use cases
    factory { DeleteTimetableCellUseCase(get()) }
    factory { InsertTimetableCellUseCase(get()) }
    factory { UpdateTimetableCellUseCase(get()) }
    factory { GetTimetableCellTypeUseCase(get()) }
    factory { SetTimetableCellTypeUseCase(get()) }

    // Open lecture use cases
    factory { GetOpenLectureListUseCase(get()) }
    factory { UpdateOpenLectureIfNeedUseCase(get())}

    factory { CheckNeedForceUpdateUseCase(get())}
}