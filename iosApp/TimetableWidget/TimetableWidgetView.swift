import SwiftUI
import WidgetKit

// Compose parity constants
private let HOUR_HEIGHT: CGFloat = 34 // fallback; actual row height computed dynamically per layout
private let HEADER_HEIGHT: CGFloat = 20
private let BORDER_WIDTH: CGFloat = 0.5
private let CONTROL_BAR_HEIGHT: CGFloat = 26
private let CONTROL_BUTTON_SIZE: CGFloat = 26
private let SPECIAL_CELL_HEIGHT: CGFloat = 24
private let MINUTE10: Int = 10
private let MINUTE60: Int = 60

// Compose design colors
private let COLOR_GRAY200 = Color(hex: "E8E9EF")
private let COLOR_GRAY400 = Color(hex: "A3A2B1")
private let COLOR_GRAYF6 = Color(hex: "F6F6F6")
private let COLOR_WHITE100 = Color(hex: "FFFFFF")

// Fallback mapping (same as provider) in case incoming color is enum string
private let TIMETABLE_COLOR_ENUM_TO_HEX: [String: String] = [
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

private func colorFromHexOrEnum(_ value: String) -> Color {
    // If it looks like hex, use it directly
    let onlyHexDigits = value.filter { c in
        ("0"..."9").contains(String(c)) || ("a"..."f").contains(String(c.lowercased()))
    }
    if onlyHexDigits.count == 6 || onlyHexDigits.count == 8 || value.lowercased().hasPrefix("0x") || value.contains("#") {
        return Color(hex: value)
    }
    // Otherwise treat as enum name
    if let mapped = TIMETABLE_COLOR_ENUM_TO_HEX[value.uppercased()] {
        return Color(hex: mapped)
    }
    // Fallback
    return COLOR_WHITE100
}

struct TimetableWidgetView: View {
    let entry: TimetableEntry

    var body: some View {
        GeometryReader { geometry in
            // Always show timetable grid, even if empty
            let timetableData = entry.timetableData ?? TimetableData(cellList: [])
            TimetableContent(
                timetableData: timetableData,
                size: geometry.size
            )
        }
    }
}

struct TimetableContent: View {
    let timetableData: TimetableData
    let size: CGSize

    private let weekdays: [TimetableDay] = [.monday, .tuesday, .wednesday, .thursday, .friday]

    private var maxDisplayPeriods: Int {
        // Saturday and e-learning cells are listed below the grid, so they must not stretch the time axis.
        let lastEnd = timetableData.cellList
            .filter { weekdays.contains($0.day) }
            .map { $0.endPeriod }
            .max() ?? 0
        return max(8, lastEnd + 1) // Align with Compose maxPeriod()
    }

    var body: some View {
        // Match Compose layout: outer rounded container with inner grid and e-learning list
        let contentWidth = max(0, size.width)
        let timeWeight: CGFloat = 0.06
        let timeWidth = contentWidth * timeWeight
        let dayWidth = (contentWidth - timeWidth) / CGFloat(weekdays.count)

        let specialCells = timetableData.cellList.filter { $0.day == .saturday || $0.day == .eLearning }

        // Dynamic hour height: allocate remaining height (excluding header + special rows) equally to all periods
        let specialHeight = CGFloat(specialCells.count) * SPECIAL_CELL_HEIGHT
        let availableGridHeight = max(0, size.height - HEADER_HEIGHT - specialHeight)
        let hourHeight = max(1, availableGridHeight / CGFloat(maxDisplayPeriods))

        let startPeriod = 1
        let endPeriod = maxDisplayPeriods

        ZStack {
            RoundedRectangle(cornerRadius: 12)
                .fill(COLOR_WHITE100)
            RoundedRectangle(cornerRadius: 12)
                .stroke(COLOR_GRAY200, lineWidth: BORDER_WIDTH)

            VStack(spacing: 0) {
                // Left: timetable content area
                VStack(spacing: 0) {
                    // Main timetable grid (Mon–Fri)
                    HStack(spacing: 0) {
                        TimeColumn(startPeriod: startPeriod, endPeriod: endPeriod, width: timeWidth, hourHeight: hourHeight)

                        ForEach(weekdays, id: \.rawValue) { day in
                            let cellsForDay = timetableData.cellList
                                .filter { $0.day == day }
                                .sorted { $0.startPeriod < $1.startPeriod }

                        ClassColumn(
                            day: day,
                            cells: cellsForDay,
                            startPeriod: startPeriod,
                            endPeriod: endPeriod,
                            width: dayWidth,
                            hourHeight: hourHeight
                        )
                        }
                    }

                    // E-learning and Saturday classes (Compose-like)
                    if !specialCells.isEmpty {
                        ForEach(Array(specialCells.enumerated()), id: \.offset) { idx, cell in
                            SpecialClassCell(cell: cell, isLast: idx == specialCells.count - 1)
                        }
                    }
                }
                .frame(width: contentWidth)
            }
            .padding(.horizontal, 0)
            // Overlay refresh control without affecting layout height
            .overlay(alignment: .bottomTrailing) {
                if #available(iOS 17.0, *) {
                    Button(intent: RefreshTimetableIntent()) {
                        Image(systemName: "arrow.clockwise")
                            .font(.system(size: 12, weight: .bold))
                            .padding(4)
                    }
                    .frame(width: CONTROL_BUTTON_SIZE, height: CONTROL_BAR_HEIGHT)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 6)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(COLOR_WHITE100.opacity(0.5))
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(COLOR_GRAY200.opacity(0.9), lineWidth: BORDER_WIDTH)
                            )
                    )
                    .padding(.horizontal, 6)
                    .padding(.bottom, 8)
                    .buttonStyle(CompactIconButtonStyle())
                }
            }
        }
    }
}

// Compact icon button style with press feedback and black icon color
struct CompactIconButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .foregroundColor(.black)
            .opacity(configuration.isPressed ? 0.6 : 1.0)
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
            .animation(.easeOut(duration: 0.12), value: configuration.isPressed)
    }
}

struct TimeColumn: View {
    let startPeriod: Int
    let endPeriod: Int
    let width: CGFloat
    let hourHeight: CGFloat

    var body: some View {
        VStack(spacing: 0) {
            // Top-left empty header cell
            Rectangle()
                .fill(COLOR_WHITE100)
                .frame(width: width, height: HEADER_HEIGHT)
                .overlay(Rectangle().stroke(COLOR_GRAY200, lineWidth: BORDER_WIDTH))

            ForEach(startPeriod...endPeriod, id: \.self) { period in
                Rectangle()
                    .fill(COLOR_WHITE100)
                    .overlay(
                        Text("\(period + 8)")
                            .font(.system(size: 11))
                            .foregroundColor(COLOR_GRAY400)
                    )
                    .frame(width: width, height: hourHeight)
                    .overlay(Rectangle().stroke(COLOR_GRAY200, lineWidth: BORDER_WIDTH))
            }
        }
    }
}

struct ClassColumn: View {
    let day: TimetableDay
    let cells: [TimetableCellData]
    let startPeriod: Int
    let endPeriod: Int
    let width: CGFloat
    let hourHeight: CGFloat

    var body: some View {
        ZStack(alignment: .topLeading) {
            // Base grid (no class coloring), keep hour separators
            VStack(spacing: 0) {
                // Day header (월, 화, ...)
                Rectangle()
                    .fill(COLOR_WHITE100)
                    .overlay(
                        Text(day.displayName)
                            .font(.system(size: 11))
                            .foregroundColor(COLOR_GRAY400)
                    )
                    .frame(width: width, height: HEADER_HEIGHT)
                    .overlay(Rectangle().stroke(COLOR_GRAY200, lineWidth: BORDER_WIDTH))

                ForEach(startPeriod...endPeriod, id: \.self) { _ in
                    Rectangle()
                        .fill(COLOR_WHITE100)
                        .frame(width: width, height: hourHeight)
                        .overlay(Rectangle().stroke(COLOR_GRAY200, lineWidth: BORDER_WIDTH))
                }
            }

            // Overlay class blocks with minute precision to avoid inner lines
            let windowStartMinute = (startPeriod + 8) * MINUTE60
            let windowEndMinute = (endPeriod + 8) * MINUTE60 + MINUTE60

            ForEach(Array(cells.sorted(by: { $0.startPeriod < $1.startPeriod }).enumerated()), id: \.offset) { _, cell in
                let startMinute = (cell.startPeriod + 8) * MINUTE60 + 3 * MINUTE10
                let endMinute = (cell.endPeriod + 9) * MINUTE60 + 2 * MINUTE10

                let visibleStart = max(startMinute, windowStartMinute)
                let visibleEnd = min(endMinute, windowEndMinute)
                let duration = max(0, visibleEnd - visibleStart)

                if duration > 0 {
                    let yOffset = CGFloat(visibleStart - windowStartMinute) / CGFloat(MINUTE60) * hourHeight + HEADER_HEIGHT
                    let blockHeight = CGFloat(duration) / CGFloat(MINUTE60) * hourHeight

                    ClassOverlayBlock(
                        cell: cell,
                        width: width,
                        height: blockHeight
                    )
                    .offset(x: 0, y: yOffset)
                }
            }
        }
    }
}

// Draws a single class block spanning multiple periods with minute precision, covering grid lines beneath
struct ClassOverlayBlock: View {
    let cell: TimetableCellData
    let width: CGFloat
    let height: CGFloat

    var body: some View {
        ZStack(alignment: .topLeading) {
            Rectangle()
                .fill(colorFromHexOrEnum(cell.color))
                .frame(width: width, height: height)
            // Render content near the top of the block
            ClassCellContentFull(cell: cell, availableHeight: height)
                .frame(width: width, height: height, alignment: .topLeading)
        }
    }
}

struct ClassCellContentFull: View {
    let cell: TimetableCellData
    var availableHeight: CGFloat? = nil

    var body: some View {
        // Approximate Compose logic for title max lines based on total block height
        let blockHeight = availableHeight ?? (CGFloat(cell.endPeriod - cell.startPeriod + 1) * HOUR_HEIGHT - 8)
        let isShort = blockHeight <= 100
        let titleMaxLines = isShort ? 1 : 2

        return VStack(alignment: .leading, spacing: 2) {
            Text(cell.name)
                .font(.system(size: 11, weight: .semibold))
                .foregroundColor(.white)
                .lineLimit(titleMaxLines)

            if !cell.professor.isEmpty {
                Text(cell.professor)
                    .font(.system(size: 9))
                    .foregroundColor(Color.white.opacity(0.9))
                    .lineLimit(1)
            }

            if !cell.location.isEmpty {
                Text(cell.location)
                    .font(.system(size: 9))
                    .foregroundColor(Color.white.opacity(0.9))
                    .lineLimit(1)
            }
        }
        .padding(4)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
    }
}

struct SpecialClassCell: View {
    let cell: TimetableCellData
    var isLast: Bool = false

    var body: some View {
        // Compose: "수업명 / 요일 (시작 - 끝)" in Gray400, white background, GrayF6 border
        let nameAndDay = "\(cell.name) / \(cell.day.displayName)"
        let text = (cell.startPeriod != 0 && cell.endPeriod != 0)
            ? nameAndDay + " (\(cell.startPeriod) - \(cell.endPeriod))"
            : nameAndDay

        // Always use square corners for special cells (including last item)
        let cornerRadius: CGFloat = 0

        VStack(alignment: .leading, spacing: 0) {
            Text(text)
                .font(.system(size: 10))
                .foregroundColor(COLOR_GRAY400)
                .lineLimit(1)
                .padding(.vertical, 8)
                .padding(.leading, 16)
                .padding(.trailing, 0)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(COLOR_WHITE100)
        .overlay(
            RoundedRectangle(cornerRadius: cornerRadius)
                .stroke(COLOR_GRAYF6, lineWidth: BORDER_WIDTH)
        )
    }
}

struct EmptyTimetableView: View {
    var body: some View {
        VStack {
            Image(systemName: "calendar")
                .font(.largeTitle)
                .foregroundColor(.gray)

            Text("시간표 없음")
                .font(.caption)
                .foregroundColor(.gray)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        // Provide refresh control even in empty state (iOS 17+)
        .overlay(alignment: .bottomTrailing) {
            if #available(iOS 17.0, *) {
                Button(intent: RefreshTimetableIntent()) {
                    Image(systemName: "arrow.clockwise")
                        .font(.system(size: 12, weight: .bold))
                        .padding(4)
                }
                .frame(width: CONTROL_BUTTON_SIZE, height: CONTROL_BAR_HEIGHT)
                .padding(.horizontal, 8)
                .padding(.vertical, 6)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(COLOR_WHITE100.opacity(0.9))
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(COLOR_GRAY200.opacity(0.9), lineWidth: BORDER_WIDTH)
                        )
                )
                .padding(.horizontal, 6)
                .padding(.bottom, 8)
                .buttonStyle(CompactIconButtonStyle())
            }
        }
    }
}

extension Color {
    init(hex: String) {
        // Keep only hex digits (0-9, a-f, A-F)
        let hexDigits = hex.filter { c in
            ("0"..."9").contains(String(c)) || ("a"..."f").contains(String(c.lowercased()))
        }
        var cleaned = hexDigits
        // Handle common prefixes like 0x or x if any remained
        if cleaned.lowercased().hasPrefix("0x") {
            cleaned = String(cleaned.dropFirst(2))
        }

        var int: UInt64 = 0
        Scanner(string: cleaned).scanHexInt64(&int)

        let a, r, g, b: UInt64
        switch cleaned.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, (int >> 8) & 0xFF, int & 0xFF)
        case 8: // ARGB or RRGGBBAA (32-bit)
            // Heuristic: prefer ARGB; if alpha seems at the end (suffix 'FF'), treat as RRGGBBAA
            let leadingAlpha = (int >> 24) & 0xFF
            let trailingAlpha = int & 0xFF
            if trailingAlpha == 0xFF && leadingAlpha != 0xFF { // likely RRGGBBAA
                (a, r, g, b) = (trailingAlpha, (int >> 24) & 0xFF, (int >> 16) & 0xFF, (int >> 8) & 0xFF)
            } else { // default ARGB
                (a, r, g, b) = ((int >> 24) & 0xFF, (int >> 16) & 0xFF, (int >> 8) & 0xFF, int & 0xFF)
            }
        default:
            (a, r, g, b) = (255, 255, 255, 255)
        }

        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}
