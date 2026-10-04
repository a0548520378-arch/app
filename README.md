# מגן מסך בנסיעה

אפליקציית Android בעברית שחוסמת מגע במסך בזמן נסיעה במהירות של 15 קמ"ש ומעלה, תוך השארת התצוגה גלויה. Waze, Google Maps ומקלדות מוחרגים מהחסימה.

## קוד ביטול חסימה ברירת מחדל
`1234`

## הרשאות נדרשות
- מיקום מדויק
- שירות נגישות (Accessibility Service)
- התראות (Android 13 ומעלה)

## בנייה
הפרויקט משתמש ב-Android Gradle Plugin 9.4 וב-Gradle 9.8. הבנייה האוטומטית ב-GitHub Actions מפיקה APK Debug ו-Release.

> הערה: Android Auto/Automotive אינם זהים לאפליקציית Android רגילה. גרסה זו מיועדת להתקנה על מכשיר Android/יחידת מולטימדיה שמריצה APK רגיל; Android Auto projection כפוף לכללי הפלטפורמה.


Build validation after Kotlin fixes: 2026-10-04.


Startup UI compilation fix validation: 2026-10-04.


Final startup build trigger after string-literal cleanup: 2026-10-04.
