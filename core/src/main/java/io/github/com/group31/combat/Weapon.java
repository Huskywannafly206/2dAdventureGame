package io.github.com.group31.combat;

/**
 * Enum định nghĩa tất cả loại vũ khí trong game.
 * Mỗi vũ khí có style tấn công (melee/ranged), sát thương, tốc độ hồi và
 * atlas key để render sprite cầm trên tay.
 */
public enum Weapon {

    // -- Cận chiến (Melee) --
    FIST(
        "Đấm", Style.MELEE,
        1f, 0.4f,
        null, null,
        null
    ),
    SWORD(
        "Kiếm", Style.MELEE,
        2f, 0.45f,
        "weapon_sword/weapon_sword",
        "weapon_sword/sprite_in_hand",
        null
    ),
    RUSTY_SWORD(
        "Kiếm gỉ sét", Style.MELEE,
        1.5f, 0.45f,
        "weapon_rusty_sword/weapon_rusty_sword",
        "weapon_rusty_sword/sprite_in_hand",
        null
    ),

    // -- Đánh xa (Ranged) --
    BOW(
        "Cung tên", Style.RANGED,
        1.5f, 0.6f,
        "weapon_bow/weapon_bow",
        null,                          // Cung không có sprite in-hand riêng
        ProjectileType.ARROW
    ),
    MAGIC_WAND(
        "Trượng phép", Style.RANGED,
        2.5f, 0.8f,
        "weapon_magicWand/weapon_magicWand",
        "weapon_magicWand/sprite_in_hand",
        ProjectileType.FIREBALL
    );

    // -------------------------------------------------------------------------

    /** Kiểu tấn công: cận chiến hay đánh xa. */
    public enum Style { MELEE, RANGED }

    /** Loại đạn bắn ra (chỉ áp dụng cho Ranged). */
    public enum ProjectileType {
        ARROW(
            "projectile_arrow/Arrow",
            12f,   // tốc độ bay
            1.5f,  // thời gian sống tối đa (giây)
            0f,    // độ lệch góc ngẫu nhiên tối đa (°) — arrow bay thẳng
            false  // không tự tìm mục tiêu
        ),
        FIREBALL(
            "projectile_fireball/Fireball",
            7f,
            2.0f,
            0f,
            true   // tự tìm mục tiêu gần nhất (auto-aim)
        );

        public final String atlasKey;
        public final float speed;
        public final float maxLifeTime;
        public final float spreadDegrees; // Chưa dùng cho Arrow/Fireball — để mở rộng sau
        public final boolean autoAim;

        ProjectileType(String atlasKey, float speed, float maxLifeTime,
                       float spreadDegrees, boolean autoAim) {
            this.atlasKey      = atlasKey;
            this.speed         = speed;
            this.maxLifeTime   = maxLifeTime;
            this.spreadDegrees = spreadDegrees;
            this.autoAim       = autoAim;
        }
    }

    // -------------------------------------------------------------------------

    public final String displayName;
    public final Style style;
    public final float damage;
    /** Thời gian hồi chiêu khi tấn công (giây). */
    public final float attackCooldown;
    /** Atlas key của sprite drop trên bản đồ (null nếu không có). */
    public final String dropAtlasKey;
    /** Atlas key của sprite cầm trên tay (null nếu vũ khí không có in-hand sprite). */
    public final String inHandAtlasKey;
    /** Loại đạn bắn ra (null nếu là melee). */
    public final ProjectileType projectileType;

    Weapon(String displayName, Style style,
           float damage, float attackCooldown,
           String dropAtlasKey, String inHandAtlasKey,
           ProjectileType projectileType) {
        this.displayName    = displayName;
        this.style          = style;
        this.damage         = damage;
        this.attackCooldown = attackCooldown;
        this.dropAtlasKey   = dropAtlasKey;
        this.inHandAtlasKey = inHandAtlasKey;
        this.projectileType = projectileType;
    }

    public boolean isMelee()  { return style == Style.MELEE; }
    public boolean isRanged() { return style == Style.RANGED; }
}
