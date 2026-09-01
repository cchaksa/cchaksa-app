import WidgetKit
import Foundation

struct TimetableEntry: TimelineEntry {
    let date: Date
    let timetableData: TimetableData?
}

struct TimetableData {
    let cellList: [TimetableCellData]
}

struct TimetableCellData {
    let name: String
    let professor: String
    let location: String
    let day: TimetableDay
    let startPeriod: Int
    let endPeriod: Int
    let color: String
}

enum TimetableDay: Int, CaseIterable {
    case monday = 0
    case tuesday = 1
    case wednesday = 2
    case thursday = 3
    case friday = 4
    case saturday = 5
    case eLearning = 6
    
    var displayName: String {
        switch self {
        case .monday: return "월"
        case .tuesday: return "화"
        case .wednesday: return "수"
        case .thursday: return "목"
        case .friday: return "금"
        case .saturday: return "토"
        case .eLearning: return "이러닝"
        }
    }
    
    var idx: Int {
        return rawValue
    }
}
