# اللعبة: الرتب والحملات

- `config/app.json` يحمل `game.ranks`: novice 1، apprentice 3، knight 5، captain 8، mage 12، sage 16، emperor 20.
- `Store.rankKey` و`nextRank` و`rankProgress` تُحسب من المستوى الحالي.
- `Campaign` باسم وهدف أيام وتاريخ آخر تسجيل، و`done` عند بلوغ الهدف.
- تسجيل اليوم يمنع التكرار في نفس اليوم، ويُحذف مع زر الحذف.
