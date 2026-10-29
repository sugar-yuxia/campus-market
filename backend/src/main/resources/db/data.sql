-- 演示数据（H2 开发环境每次启动自动执行；MySQL 环境首次初始化时手动执行一次）
-- 演示账号：demo / 123456（买家），seller / 123456（卖家）

INSERT INTO categories (id, name, sort_order) VALUES
    (1, '教材书籍', 1),
    (2, '数码电子', 2),
    (3, '生活用品', 3),
    (4, '服饰鞋包', 4),
    (5, '运动器材', 5),
    (6, '其他', 6);

INSERT INTO users (id, username, password, nickname, phone, created_at) VALUES
    (1001, 'demo', '$2b$10$WQog7XFOBAE1WcQ5/SNFm.ho59O8Jr9wxswW0LahfY1vWgJhPVRJ6', '校园小买家', '13800000001', '2026-09-01 09:00:00'),
    (1002, 'seller', '$2b$10$WQog7XFOBAE1WcQ5/SNFm.ho59O8Jr9wxswW0LahfY1vWgJhPVRJ6', '清仓学长', '13800000002', '2026-09-02 10:00:00');

INSERT INTO products (id, title, description, price, original_price, category_id, seller_id, cover, condition_level, status, view_count, created_at) VALUES
    (1001, '《高等数学》同济第八版 上下册', '考研复习用过，书内有少量铅笔笔记，不影响阅读，可小刀。', 18.00, 55.00, 1, 1002,
     'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=second-hand%20calculus%20textbook%20open%20on%20a%20wooden%20desk%2C%20warm%20lighting&image_size=landscape_4_3', 8, 'ON_SALE', 156, '2026-10-20 09:10:00'),
    (1002, '罗技 K845 机械键盘 九成新', '青轴手感清脆，宿舍自用半年，键帽无打油，附原装包装盒。', 129.00, 349.00, 2, 1002,
     'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=black%20mechanical%20keyboard%20with%20backlight%20on%20desk&image_size=landscape_4_3', 9, 'ON_SALE', 203, '2026-10-21 14:20:00'),
    (1003, '21 速山地自行车 代步通勤', '毕业出，车况良好，刹车和变速都正常，送车锁。', 260.00, 899.00, 5, 1002,
     'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=mountain%20bicycle%20parked%20on%20campus%20path&image_size=landscape_4_3', 7, 'ON_SALE', 188, '2026-10-22 08:30:00'),
    (1004, '宿舍 LED 护眼台灯', '三档亮度，USB 供电，用了一个学期，包装完整。', 25.00, 79.00, 3, 1002,
     'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20white%20led%20desk%20lamp%20on%20study%20desk&image_size=landscape_4_3', 9, 'ON_SALE', 121, '2026-10-23 19:00:00'),
    (1005, '尤尼克斯羽毛球拍 单拍', '拉线 26 磅，附手胶和拍套，适合入门球友。', 95.00, 260.00, 5, 1002,
     'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=badminton%20racket%20and%20shuttlecock%20on%20court&image_size=landscape_4_3', 8, 'ON_SALE', 96, '2026-10-24 11:45:00'),
    (1006, '索尼 WH-1000XM4 降噪耳机', '音质和降噪都很好，已过保，配件齐全，面交可试听。', 620.00, 1899.00, 2, 1002,
     'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=black%20over-ear%20noise%20cancelling%20headphones&image_size=landscape_4_3', 9, 'ON_SALE', 245, '2026-10-25 16:00:00'),
    (1007, '全新帆布鞋 42 码', '买大了一码，仅试穿，鞋盒完好，可退换凭证齐全。', 45.00, 159.00, 4, 1002,
     'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=pair%20of%20white%20canvas%20sneakers&image_size=landscape_4_3', 10, 'ON_SALE', 78, '2026-10-26 10:10:00'),
    (1008, '500ml 不锈钢保温杯', '保温效果不错，杯身有轻微使用痕迹，已清洗消毒。', 12.00, 59.00, 3, 1002,
     'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=stainless%20steel%20insulated%20water%20bottle&image_size=landscape_4_3', 8, 'ON_SALE', 64, '2026-10-27 09:00:00'),
    (1009, '考研英语真题解析（近十年）', '只做了两套，其余全新，适合 2027 考研党。', 9.90, 30.00, 1, 1002,
     'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=stack%20of%20english%20exam%20preparation%20books&image_size=landscape_4_3', 9, 'ON_SALE', 39, '2026-10-27 20:30:00'),
    (1010, '便携蓝牙音箱（已下架）', '音质尚可，续航约 6 小时，暂不出了。', 55.00, 199.00, 2, 1002,
     'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=portable%20bluetooth%20speaker%20on%20table&image_size=landscape_4_3', 7, 'OFF_SHELF', 52, '2026-10-19 15:00:00');

INSERT INTO product_images (product_id, url, sort_order) VALUES
    (1001, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=second-hand%20calculus%20textbook%20open%20on%20a%20wooden%20desk%2C%20warm%20lighting&image_size=landscape_4_3', 0),
    (1001, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=used%20college%20textbooks%20stacked%20on%20desk&image_size=landscape_4_3', 1),
    (1002, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=black%20mechanical%20keyboard%20with%20backlight%20on%20desk&image_size=landscape_4_3', 0),
    (1002, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=mechanical%20keyboard%20close-up%20keycaps&image_size=landscape_4_3', 1),
    (1003, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=mountain%20bicycle%20parked%20on%20campus%20path&image_size=landscape_4_3', 0),
    (1003, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=bicycle%20wheel%20detail%20close-up&image_size=landscape_4_3', 1),
    (1004, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20white%20led%20desk%20lamp%20on%20study%20desk&image_size=landscape_4_3', 0),
    (1005, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=badminton%20racket%20and%20shuttlecock%20on%20court&image_size=landscape_4_3', 0),
    (1006, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=black%20over-ear%20noise%20cancelling%20headphones&image_size=landscape_4_3', 0),
    (1007, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=pair%20of%20white%20canvas%20sneakers&image_size=landscape_4_3', 0),
    (1008, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=stainless%20steel%20insulated%20water%20bottle&image_size=landscape_4_3', 0),
    (1009, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=stack%20of%20english%20exam%20preparation%20books&image_size=landscape_4_3', 0),
    (1010, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=portable%20bluetooth%20speaker%20on%20table&image_size=landscape_4_3', 0);
