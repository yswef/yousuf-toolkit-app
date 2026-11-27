-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
ALTER TABLE journal
    ADD COLUMN mood VARCHAR(16) NOT NULL DEFAULT '',
    ADD COLUMN background VARCHAR(40) NOT NULL DEFAULT 'cream',
    ADD COLUMN rich_body LONGTEXT NULL,
    ADD COLUMN font_name VARCHAR(40) NOT NULL DEFAULT 'Cairo',
    ADD COLUMN colors VARCHAR(160) NOT NULL DEFAULT '',
    ADD COLUMN drawings LONGTEXT NULL,
    ADD COLUMN stickers LONGTEXT NULL,
    ADD COLUMN images LONGTEXT NULL,
    ADD COLUMN emoji_pack VARCHAR(40) NOT NULL DEFAULT 'free-1',
    ADD COLUMN sticker_pack VARCHAR(40) NOT NULL DEFAULT 'free-1',
    ADD COLUMN voice_path VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN has_time TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN synced_at BIGINT NOT NULL DEFAULT 0;
-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
