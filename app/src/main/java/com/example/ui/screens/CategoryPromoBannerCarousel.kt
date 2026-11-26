package com.example.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.SubscriptionFeatureFlags
import com.example.ui.theme.DeepTeal
import com.example.ui.theme.StarGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Immutable
data class CategoryPromoSlideData(
  val id: String,
  val eyebrow: String,
  val headline: String,
  val description: String,
  val ctaLabel: String,
  @param:DrawableRes val imageRes: Int,
  val action: CategoryPromoAction,
)

enum class CategoryPromoAction {
  EXPLORE_SERVICES,
  FIND_PROFESSIONAL,
  JOIN_SKILLCONNECT,
  FIND_NEARBY_SERVICES,
  EXPLORE_PRO_PLAN,
}

object CategoryPromoSlides {
  val defaults: List<CategoryPromoSlideData> =
    listOf(
      CategoryPromoSlideData(
        id = "slide_1_skilled_pros",
        eyebrow = "Find Skilled Professionals",
        headline = "Find the Right Professional!",
        description = "Discover skilled and experienced professionals near your location.",
        ctaLabel = "Explore Services",
        imageRes = R.drawable.img_cat_slide_1,
        action = CategoryPromoAction.EXPLORE_SERVICES,
      ),
      CategoryPromoSlideData(
        id = "slide_2_home_repair",
        eyebrow = "Need a Home Repair?",
        headline = "Your Home, Our Professionals.",
        description =
          "Find electricians, plumbers, carpenters and other home service experts.",
        ctaLabel = "Find a Professional",
        imageRes = R.drawable.img_cat_slide_2,
        action = CategoryPromoAction.FIND_PROFESSIONAL,
      ),
      CategoryPromoSlideData(
        id = "slide_3_grow_business",
        eyebrow = "Grow Your Business",
        headline = "Turn Your Skills Into Opportunities!",
        description =
          "Register as a service provider and connect with customers in your area.",
        ctaLabel = "Join SkillConnect",
        imageRes = R.drawable.img_cat_slide_3,
        action = CategoryPromoAction.JOIN_SKILLCONNECT,
      ),
      CategoryPromoSlideData(
        id = "slide_4_nearby_services",
        eyebrow = "Find Services Near You",
        headline = "Local Services Made Easy.",
        description =
          "Search nearby technicians, mechanics, teachers, gardeners and more.",
        ctaLabel = "Find Nearby Services",
        imageRes = R.drawable.img_cat_slide_4,
        action = CategoryPromoAction.FIND_NEARBY_SERVICES,
      ),
      CategoryPromoSlideData(
        id = "slide_5_unlimited_bookings",
        eyebrow =
          if (SubscriptionFeatureFlags.PRO_PLAN_ENABLED) "Unlimited Booking Opportunities"
          else "Unlimited Free Bookings",
        headline =
          if (SubscriptionFeatureFlags.PRO_PLAN_ENABLED) "Ready for More Bookings?"
          else "All Bookings Recorded Free!",
        description =
          if (SubscriptionFeatureFlags.PRO_PLAN_ENABLED)
            "Explore SkillConnect Pro and enjoy unlimited service bookings."
          else
            "Accept unlimited service bookings on your Free Account with full history.",
        ctaLabel =
          if (SubscriptionFeatureFlags.PRO_PLAN_ENABLED) "Explore Pro Plan"
          else "Explore Services",
        imageRes = R.drawable.img_cat_slide_5,
        action = CategoryPromoAction.EXPLORE_PRO_PLAN,
      ),
    )
}

@Composable
fun CategoryPromoBannerCarousel(
  onSlideAction: (CategoryPromoAction) -> Unit,
  modifier: Modifier = Modifier,
  slides: List<CategoryPromoSlideData> = CategoryPromoSlides.defaults,
) {
  if (slides.isEmpty()) return

  val pagerState = rememberPagerState(pageCount = { slides.size })
  val scope = rememberCoroutineScope()
  val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
  var interactionNonce by remember { mutableIntStateOf(0) }

  // Auto-advance every 4 seconds; pause while dragging/animating and resume cleanly afterward
  LaunchedEffect(pagerState, isDragged, pagerState.isScrollInProgress, interactionNonce, slides.size) {
    if (!isDragged && !pagerState.isScrollInProgress) {
      delay(4000L)
      val nextPage = (pagerState.currentPage + 1) % slides.size
      pagerState.animateScrollToPage(nextPage)
    }
  }

  Column(
    modifier = modifier.fillMaxWidth().testTag("category_promo_carousel"),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    // Exact homepage promotional banner dimensions: 148.dp height, 18.dp corners, 16.dp horizontal padding, 12.dp pageSpacing
    HorizontalPager(
      state = pagerState,
      contentPadding = PaddingValues(horizontal = 16.dp),
      pageSpacing = 12.dp,
      modifier = Modifier.fillMaxWidth().testTag("category_promo_pager"),
    ) { page ->
      val slide = slides[page]
      CategoryPromoSlideCard(
        slide = slide,
        onCtaClick = {
          interactionNonce++
          onSlideAction(slide.action)
        },
        testTag = "category_promo_slide_${page + 1}",
      )
    }

    // Five Page Indicator Dots directly beneath banner (matches main page)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      repeat(slides.size) { index ->
        val selected = pagerState.currentPage == index
        Box(
          modifier =
            Modifier.padding(horizontal = 4.dp)
              .size(width = if (selected) 10.dp else 8.dp, height = 8.dp)
              .clip(CircleShape)
              .background(if (selected) DeepTeal else MaterialTheme.colorScheme.outline)
              .clickable {
                interactionNonce++
                scope.launch { pagerState.animateScrollToPage(index) }
              }
              .testTag("category_promo_dot_${index + 1}")
        )
      }
    }
  }
}

@Composable
private fun CategoryPromoSlideCard(
  slide: CategoryPromoSlideData,
  onCtaClick: () -> Unit,
  testTag: String,
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    modifier = Modifier.fillMaxWidth().height(148.dp).testTag(testTag),
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(
            Brush.horizontalGradient(
              colors = listOf(Color(0xFF0C343A), Color(0xFF144E54), Color(0xFF1A625B))
            )
          )
    ) {
      // Same sweeping emerald ribbon behind the right-curved graphic as homepage HeroPromoSlide
      Canvas(modifier = Modifier.matchParentSize()) {
        val ribbon =
          Path().apply {
            moveTo(size.width * 0.50f, size.height)
            quadraticTo(
              size.width * 0.56f,
              size.height * 0.32f,
              size.width * 0.72f,
              0f,
            )
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            close()
          }
        drawPath(
          path = ribbon,
          brush =
            Brush.verticalGradient(
              colors = listOf(Color(0xFF2AC466), Color(0xFF20A554))
            ),
        )
      }

      val rightArcShape = remember {
        GenericShape { size, _ ->
          moveTo(size.width * 0.22f, 0f)
          lineTo(size.width, 0f)
          lineTo(size.width, size.height)
          lineTo(0f, size.height)
          quadraticTo(size.width * 0.04f, size.height * 0.36f, size.width * 0.22f, 0f)
          close()
        }
      }

      Image(
        painter = painterResource(id = slide.imageRes),
        contentDescription = slide.eyebrow,
        contentScale = ContentScale.Crop,
        alignment = Alignment.TopCenter,
        modifier =
          Modifier.fillMaxHeight()
            .width(158.dp)
            .align(Alignment.CenterEnd)
            .clip(rightArcShape),
      )

      Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(modifier = Modifier.fillMaxWidth(0.59f)) {
          Text(
            text = slide.eyebrow,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = StarGold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Spacer(modifier = Modifier.height(1.dp))
          Text(
            text = slide.headline,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            lineHeight = 18.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = slide.description,
            fontSize = 10.5.sp,
            color = Color.White.copy(alpha = 0.92f),
            lineHeight = 13.5.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }

        Button(
          onClick = onCtaClick,
          shape = RoundedCornerShape(50),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = Color(0xFF27B75E),
              contentColor = Color.White,
            ),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
          modifier = Modifier.height(32.dp).testTag("${testTag}_cta"),
        ) {
          Text(
            text = slide.ctaLabel,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
          )
          Spacer(modifier = Modifier.width(5.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
          )
        }
      }
    }
  }
}
