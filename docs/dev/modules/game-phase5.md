<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# اللعبة: الرتب والحملات

- `config/app.json` يحمل `game.ranks`: novice 1، apprentice 3، knight 5، captain 8، mage 12، sage 16، emperor 20.
- `Store.rankKey` و`nextRank` و`rankProgress` تُحسب من المستوى الحالي.
- `Campaign` باسم وهدف أيام وتاريخ آخر تسجيل، و`done` عند بلوغ الهدف.
- تسجيل اليوم يمنع التكرار في نفس اليوم، ويُحذف مع زر الحذف.
