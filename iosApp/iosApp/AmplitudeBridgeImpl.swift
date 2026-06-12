import Foundation
import ComposeApp
import AmplitudeSwift

class AmplitudeBridgeImpl: AmplitudeBridge {

    private let amplitude: Amplitude

    init() {
        #if DEBUG
        let apiKey = Bundle.main.object(forInfoDictionaryKey: "AMPLITUDE_API_KEY_DEV") as? String ?? ""
        #else
        let apiKey = Bundle.main.object(forInfoDictionaryKey: "AMPLITUDE_API_KEY_PROD") as? String ?? ""
        #endif

        amplitude = Amplitude(
            configuration: Configuration(
                apiKey: apiKey,
                autocapture: []
            )
        )
    }

    func setUserId(userId: String?) {
        amplitude.setUserId(userId: userId)
    }

    func track(eventType: String, properties: [String: Any]) {
        let eventProperties = properties.filter { !($0.value is NSNull) }
        amplitude.track(eventType: eventType, eventProperties: eventProperties)
    }

    func setUserProperties(properties: [String: Any]) {
        let identify = Identify()
        for (key, value) in properties {
            if value is NSNull { continue }
            identify.set(property: key, value: value)
        }
        amplitude.identify(identify: identify)
    }
}
