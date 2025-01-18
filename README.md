# ChronoFit

ChronoFit is an innovative Android application that combines physical activity tracking with digital wellbeing management. It introduces a unique approach to managing screen time by requiring users to perform push-ups to earn screen time credits for specific applications.

## Background

In today's digital age, excessive screen time has become a significant concern, often leading to reduced physical activity and potential health issues. ChronoFit addresses this challenge by:
- Encouraging regular physical exercise
- Promoting mindful app usage
- Creating a healthy balance between digital engagement and physical activity

## How It Works

1. **Earn Credits Through Exercise**
    - Users perform push-ups which are automatically detected using the device's camera
    - Each correctly performed push-up earns screen time credits (default: 2 minutes per push-up)
    - The app uses MediaPipe for accurate push-up detection and counting

2. **App Usage Management**
    - Users can select multiple applications to monitor
    - Screen time is deducted in real-time while using monitored apps
    - When credits run out, monitored apps are automatically closed

3. **Real-time Monitoring**
    - Tracks app usage in the background
    - Displays remaining credit time
    - Automatically enforces usage limits when credits are depleted

## Required Permissions

The app requires several permissions to function properly:

1. **Camera Permission**
    - Required for push-up detection
    - Used only during exercise tracking

2. **Usage Access**
    - Needed to monitor app usage time
    - Enables screen time tracking functionality

3. **Accessibility Service**
    - Required to enforce app usage limits
    - Enables automatic app closing when credits are depleted

4. **Overlay Permission**
    - Allows display of blocking overlay when credits run out
    - Shows exercise reminder notifications

5. **Install Unknown Apps**
    - Required for complete package querying
    - Enables comprehensive app selection functionality

## Technical Features

- **Push-up Detection**: Utilizes MediaPipe for accurate pose detection and exercise counting
- **Background Monitoring**: Efficient battery usage while tracking app usage
- **Multiple App Support**: Can monitor and manage multiple applications simultaneously
- **Persistent Storage**: Saves settings and credits across device restarts
- **Real-time Enforcement**: Immediate action when usage limits are reached

## Setup Instructions

1. Install the application
2. Grant all required permissions:
    - Open Settings > Apps > ChronoFit > Permissions
    - Enable Camera permission
    - Enable Usage Access
    - Enable Accessibility Service
    - Allow display over other apps
    - Allow installation from unknown sources
3. Select applications to monitor in Settings
4. Configure minutes earned per push-up
5. Start exercising to earn credits

## Important Notes

- The app requires physical movement in front of the camera for push-up detection
- Good lighting conditions improve push-up detection accuracy
- Battery optimization should be disabled for reliable background monitoring
- The app must remain installed for usage restrictions to remain active

## Privacy Considerations

ChronoFit respects user privacy:
- Camera usage only during exercise sessions
- No data transmitted to external servers
- All settings and usage data stored locally
- No personal information collected

## Minimum Requirements

- Android 7.0 (API Level 24) or higher
- Front-facing camera
- Sufficient storage for app installation
- Google Play Services for ML capabilities

## Future Enhancements

- Additional exercise types support
- Custom credit earning rates
- Detailed usage statistics
- Social features and challenges
- More granular time management options

## Support

For issues, questions, or suggestions, please create an issue in the repository or contact the development team.

---

**Note**: This application is designed to promote healthy habits and should be used responsibly. Users should ensure all permissions are granted for optimal functionality.