import WidgetKit
import SwiftUI

struct TimetableTimelineProvider: TimelineProvider {

    func placeholder(in context: Context) -> TimetableEntry {
        TimetableEntry(date: Date(), timetableData: createSampleTimetableData())
    }

    func getSnapshot(in context: Context, completion: @escaping (TimetableEntry) -> Void) {
        let entry = TimetableEntry(date: Date(), timetableData: loadTimetableData())
        completion(entry)
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<TimetableEntry>) -> Void) {
        let currentDate = Date()
        let timetableData = loadTimetableData()
        let nextUpdateDate = Calendar.current.date(byAdding: .hour, value: 1, to: currentDate) ?? currentDate

        let entries: [TimetableEntry] = [
            TimetableEntry(date: currentDate, timetableData: timetableData)
        ]

        let timeline = Timeline(entries: entries, policy: .after(nextUpdateDate))
        completion(timeline)
    }

    private func loadTimetableData() -> TimetableData? {
        if let sharedDefaults = UserDefaults(suiteName: "group.com.kunize.uswtimetable.shared"),
           let timetableJson = sharedDefaults.string(forKey: "timetable_data") {
            return parseTimetableJson(timetableJson)
        }
        return nil
    }

    private func parseTimetableJson(_ json: String) -> TimetableData? {
        guard let data = json.data(using: .utf8) else { return nil }

        do {
            guard let dict = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any] else {
                return nil
            }

            // Allow cellList to be missing or empty - default to empty array
            let cellList = (dict["cellList"] as? [[String: Any]]) ?? []

            let cells: [TimetableCellData] = cellList.compactMap { cellDict in
                guard let name = cellDict["name"] as? String else { return nil }

                let dayValue = cellDict["day"]
                let day = mapDay(dayValue) ?? .eLearning

                let professor = (cellDict["professor"] as? String) ?? ""
                let location = (cellDict["location"] as? String) ?? ""

                let colorValue = cellDict["color"]
                let colorHex = mapColorHex(colorValue) ?? "808095" // GRAY

                if day == .eLearning {
                    let startPeriod = (cellDict["startPeriod"] as? Int) ?? 0
                    let endPeriod = (cellDict["endPeriod"] as? Int) ?? 0
                    return TimetableCellData(
                        name: name,
                        professor: professor,
                        location: location,
                        day: day,
                        startPeriod: startPeriod,
                        endPeriod: endPeriod,
                        color: colorHex
                    )
                } else {
                    guard let startPeriod = cellDict["startPeriod"] as? Int,
                          let endPeriod = cellDict["endPeriod"] as? Int else {
                        return nil
                    }
                    return TimetableCellData(
                        name: name,
                        professor: professor,
                        location: location,
                        day: day,
                        startPeriod: startPeriod,
                        endPeriod: endPeriod,
                        color: colorHex
                    )
                }
            }

            return TimetableData(cellList: cells)
        } catch {
            return nil
        }
    }

    private func mapDay(_ value: Any?) -> TimetableDay? {
        if let idx = value as? Int { return TimetableDay(rawValue: idx) }
        if let str = value as? String {
            switch str.uppercased() {
            case "MON": return .monday
            case "TUE": return .tuesday
            case "WED": return .wednesday
            case "THU": return .thursday
            case "FRI": return .friday
            case "SAT": return .saturday
            case "E_LEARNING": return .eLearning
            default: return .eLearning
            }
        }
        return .eLearning
    }

    private func mapColorHex(_ value: Any?) -> String? {
        if let dict = value as? [String: Any] {
            if let hex = dict["hex"] as? String { return sanitizeHex(hex) }
            if let name = dict["name"] as? String {
                return timetableColorEnumToHex[name.uppercased()]
            }
            if let num = dict["hex"] as? NSNumber {
                return hexFromInt(num.intValue)
            }
            if let v = dict["value"] as? NSNumber {
                return hexFromInt(v.intValue)
            }
        }
        if let str = value as? String {
            let clean = sanitizeHex(str)
            if clean.count == 6 || clean.count == 8 { return clean }
            if let mapped = timetableColorEnumToHex[str.uppercased()] { return mapped }
        }
        if let num = value as? NSNumber {
            return hexFromInt(num.intValue)
        }
        return nil
    }

    private func hexFromInt(_ intColor: Int) -> String? {
        let u = UInt32(bitPattern: Int32(intColor))
        let hex = String(format: "%08X", u)
        return sanitizeHex(hex)
    }

    private func sanitizeHex(_ hex: String) -> String {
        let digits = hex.filter { c in
            ("0"..."9").contains(String(c)) || ("a"..."f").contains(String(c.lowercased()))
        }
        var clean = digits
        if clean.lowercased().hasPrefix("0x") {
            clean = String(clean.dropFirst(2))
        }
        if clean.count == 8, clean.hasPrefix("FF") {
            return String(clean.suffix(6))
        }
        return clean
    }

    private let timetableColorEnumToHex: [String: String] = [
        "BROWN": "FBC1CB",
        "BROWN_LIGHT": "F6ADB9",
        "ORANGE": "F790A1",
        "APRICOT": "F17085",
        "PURPLE": "E15F74",
        "PURPLE_LIGHT": "B0C1E9",
        "RED_LIGHT": "90A9E3",
        "PINK": "6C8BD5",
        "BROWN_DARK": "5574BF",
        "GREEN_DARK": "4662A4",
        "GREEN": "CBC6F8",
        "GREEN_LIGHT": "B9B1FC",
        "NAVY_DARK": "A298FB",
        "NAVY": "887CF0",
        "NAVY_LIGHT": "766CD4",
        "VIOLET": "BDBCC7",
        "GRAY_DARK": "ACACBB",
        "GRAY": "808095",
        "SKY": "5E5E6E",
        "VIOLET_LIGHT": "4E4E5A",
    ]

    private func createSampleTimetableData() -> TimetableData {
        return TimetableData(cellList: [
            TimetableCellData(
                name: "샘플 수업",
                professor: "홍길동",
                location: "공학관 101",
                day: .monday,
                startPeriod: 1,
                endPeriod: 2,
                color: "FBC1CB"
            )
        ])
    }
}