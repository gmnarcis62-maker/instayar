package red.line.instayar.data.model

data class Template(
    val id: String,
    val title: String,
    val category: String,
    val format: TemplateFormat,
    val isVip: Boolean,
    val tags: List<String> = emptyList(),
    val background: TemplateBackground,
    val elements: List<TemplateElement> = emptyList()
) {
    val aspectRatioFloat: Float
        get() = when (format) {
            TemplateFormat.STORY -> 9f / 16f
            TemplateFormat.POST_PORTRAIT -> 4f / 5f
            TemplateFormat.POST_SQUARE -> 1f
            TemplateFormat.HIGHLIGHT -> 1f
        }
}

enum class TemplateFormat(val titleFa: String, val ratioWidth: Int, val ratioHeight: Int) {
    STORY("استوری", 9, 16),
    POST_PORTRAIT("پست عمودی", 4, 5),
    POST_SQUARE("پست مربعی", 1, 1),
    HIGHLIGHT("کاور هایلایت", 1, 1);

    companion object {
        fun fromString(value: String): TemplateFormat {
            return when (value.uppercase()) {
                "STORY" -> STORY
                "POST_PORTRAIT" -> POST_PORTRAIT
                "POST_SQUARE" -> POST_SQUARE
                "HIGHLIGHT" -> HIGHLIGHT
                else -> STORY
            }
        }
    }
}

data class TemplateBackground(
    val type: String = "GRADIENT", // SOLID, GRADIENT
    val color: String = "#1A102F",
    val gradientColors: List<String> = listOf("#2E0854", "#0C071E"),
    val angle: Float = 45f
)

data class TemplateElement(
    val id: String,
    val type: ElementType,
    val x: Float, // 0.0f - 1.0f relative to canvas width
    val y: Float, // 0.0f - 1.0f relative to canvas height
    val width: Float, // 0.0f - 1.0f
    val height: Float, // 0.0f - 1.0f
    val rotation: Float = 0f,
    val text: String? = null,
    val fontSize: Float = 16f,
    val fontWeight: String = "BOLD", // NORMAL, MEDIUM, BOLD, BLACK
    val textColor: String = "#FFFFFF",
    val textAlign: String = "CENTER", // CENTER, RIGHT, LEFT
    val backgroundColor: String? = null,
    val shapeType: String = "ROUNDED_RECT", // RECTANGLE, ROUNDED_RECT, CIRCLE, TAG, RIBBON
    val cornerRadius: Float = 16f,
    val borderColor: String? = null,
    val borderWidth: Float = 0f,
    val badgeIcon: String? = null, // DISCOUNT, STAR, CROWN, FIRE, CHECK
    val placeholderLabel: String? = null,
    val hasShadow: Boolean = false,
    val shadowColor: String = "#80000000"
)

enum class ElementType {
    TEXT,
    IMAGE_PLACEHOLDER,
    SHAPE,
    BADGE,
    STICKER,
    BRAND_LOGO,
    BRAND_NAME,
    BRAND_CONTACT;

    companion object {
        fun fromString(value: String): ElementType {
            return when (value.uppercase()) {
                "TEXT" -> TEXT
                "IMAGE_PLACEHOLDER" -> IMAGE_PLACEHOLDER
                "SHAPE" -> SHAPE
                "BADGE" -> BADGE
                "STICKER" -> STICKER
                "BRAND_LOGO" -> BRAND_LOGO
                "BRAND_NAME" -> BRAND_NAME
                "BRAND_CONTACT" -> BRAND_CONTACT
                else -> TEXT
            }
        }
    }
}

data class BrandProfile(
    val brandName: String = "فروشگاه آنلاین من",
    val instagramHandle: String = "@my_shop_page",
    val phoneNumber: String = "۰۹۱۲۳۴۵۶۷۸۹",
    val websiteOrAddress: String = "تهران، بازار بزرگ",
    val primaryColorHex: String = "#7C3AED",
    val secondaryColorHex: String = "#EC4899",
    val logoUri: String? = null
)
