import WidgetKit
import SwiftUI

@main
struct TimetableWidgetBundle: WidgetBundle {
    var body: some Widget {
        TimetableWidget()
    }
}

struct TimetableWidget: Widget {
    let kind: String = "TimetableWidget"
    
    var body: some WidgetConfiguration {
        if #available(iOS 17.0, *) {
            return StaticConfiguration(kind: kind, provider: TimetableTimelineProvider()) { entry in
                TimetableWidgetView(entry: entry)
                    .containerBackground(.fill.tertiary, for: .widget)
            }
            .contentMarginsDisabled() // remove default widget inner margins (iOS 17+)
            .configurationDisplayName("시간표")
            .description("척척학사 시간표를 확인하세요.")
            .supportedFamilies([.systemLarge])
        } else {
            return StaticConfiguration(kind: kind, provider: TimetableTimelineProvider()) { entry in
                // Avoid extra padding to minimize whitespace on iOS 16
                TimetableWidgetView(entry: entry)
            }
            .configurationDisplayName("시간표")
            .description("척척학사 시간표를 확인하세요.")
            .supportedFamilies([.systemLarge])
        }
    }
}

struct TimetableWidget_Previews: PreviewProvider {
    static var previews: some View {
        TimetableWidgetView(entry: TimetableEntry(date: Date(), timetableData: createSampleData()))
            .previewContext(WidgetPreviewContext(family: .systemMedium))
    }
    
    static func createSampleData() -> TimetableData {
        return TimetableData(cellList: [
            TimetableCellData(
                name: "컴퓨터구조",
                professor: "김교수",
                location: "공학관 301",
                day: .monday,
                startPeriod: 1,
                endPeriod: 2,
                color: "FBC1CB"
            ),
            TimetableCellData(
                name: "자료구조",
                professor: "이교수",
                location: "공학관 205",
                day: .tuesday,
                startPeriod: 3,
                endPeriod: 4,
                color: "90A9E3"
            ),
            TimetableCellData(
                name: "온라인강의",
                professor: "박교수",
                location: "온라인",
                day: .eLearning,
                startPeriod: 1,
                endPeriod: 3,
                color: "CBC6F8"
            )
        ])
    }
}
