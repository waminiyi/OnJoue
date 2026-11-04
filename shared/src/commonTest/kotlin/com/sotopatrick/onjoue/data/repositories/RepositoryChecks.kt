package com.sotopatrick.onjoue.data.repositories

import com.sotopatrick.onjoue.data.models.activity.*
import com.sotopatrick.onjoue.data.models.common.AppException
import com.sotopatrick.onjoue.data.models.common.Url
import com.sotopatrick.onjoue.data.models.equipment.Equipment
import com.sotopatrick.onjoue.data.models.equipment.EquipmentId
import com.sotopatrick.onjoue.data.models.favorite.*
import com.sotopatrick.onjoue.data.models.review.*
import com.sotopatrick.onjoue.data.models.tag.*
import com.sotopatrick.onjoue.data.repositories.activity.RoomActivityRepository
import com.sotopatrick.onjoue.data.repositories.equipment.RoomEquipmentRepository
import com.sotopatrick.onjoue.data.repositories.favorite.RoomFavoriteRepository
import com.sotopatrick.onjoue.data.repositories.review.RoomReviewRepository
import com.sotopatrick.onjoue.data.repositories.tag.RoomTagRepository
import com.sotopatrick.onjoue.data.sources.room.AppDatabase
import kotlin.test.*
import kotlin.uuid.Uuid
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withTimeout

internal fun sampleTag(
    label: String,
    group: TagGroup,
    parent: TagId? = null,
    id: TagId = TagId(Uuid.random().toString()),
) = Tag(id, group, label, parent)

internal fun sampleEquipment(
    label: String,
    id: EquipmentId = EquipmentId(Uuid.random().toString()),
) = Equipment(id, label)

internal fun sampleActivity() = Activity(
    id = ActivityId("activity"), name = "Jeu", description = "Description",
    steps = listOf(Step("Deuxième"), Step("Première", 42)),
    variants = listOf("Z", "A", "Z"), note = "Conseil",
    age = AgeRange(AgeMonths(12), AgeMonths(60)), duration = DurationMinutes(15),
    image = Url("https://example.org/image"), document = Url("https://example.org/document"),
)

internal suspend fun verifyRepositoryRelations(database: AppDatabase) {
    val tags = RoomTagRepository(database, true)
    val equipment = RoomEquipmentRepository(database, true)
    val activities = RoomActivityRepository(database, true)
    val reviews = RoomReviewRepository(database)
    val favorites = RoomFavoriteRepository(database)
    val tag = sampleTag("Calme", TagGroup.MOOD).also { tags.save(it) }
    val item = sampleEquipment("Ballon").also { equipment.save(it) }
    var activity = sampleActivity().copy(tags = setOf(tag), equipment = setOf(item))
    activities.save(activity)
    val hydrated = assertNotNull(activities.getActivity(activity.id))
    assertEquals(activity, hydrated)
    assertEquals(tag.label, hydrated.tags.single().label)
    assertEquals(tag.group, hydrated.tags.single().group)
    assertEquals(item.label, hydrated.equipment.single().label)
    assertEquals(listOf(activity), activities.observeActivities().first())
    assertTrue(activities.isTagUsed(tag.id))
    assertTrue(activities.isEquipmentUsed(item.id))
    val renamedTag = tag.copy(label = "Très calme")
    val renamedEquipment = item.copy(label = "Grand ballon")
    coroutineScope {
        val initialEmission = CompletableDeferred<Unit>()
        val observed = async(Dispatchers.Default) {
            withTimeout(10_000) {
                activities.observeActivities().onEach { initialEmission.complete(Unit) }.first { rows ->
                    rows.singleOrNull()?.let {
                        it.tags == setOf(renamedTag) && it.equipment == setOf(renamedEquipment)
                    } == true
                }
            }
        }
        initialEmission.await()
        tags.save(renamedTag)
        equipment.save(renamedEquipment)
        assertEquals(
            listOf(activity.copy(tags = setOf(renamedTag), equipment = setOf(renamedEquipment))),
            observed.await(),
        )
    }
    assertEquals(tag.id, renamedTag.id)
    assertEquals(item.id, renamedEquipment.id)
    val renamedActivity = activity.copy(tags = setOf(renamedTag), equipment = setOf(renamedEquipment))
    assertEquals(renamedActivity, activities.getActivity(activity.id))
    activities.save(activity)
    assertEquals(renamedActivity, activities.getActivity(activity.id))
    activity = renamedActivity
    assertFailsWith<AppException.InUse> { tags.deleteTag(tag.id) }
    assertFailsWith<AppException.InUse> { equipment.deleteEquipment(item.id) }
    activities.save(activity.copy(name = "Changed"))
    assertEquals(activity.copy(name = "Changed"), activities.getActivity(activity.id))
    activities.save(activity)
    val invalidUpdate = activity.copy(name = "Changed", equipment = setOf(sampleEquipment("Absent", EquipmentId("missing"))))
    assertFailsWith<AppException.InvalidReference> { activities.save(invalidUpdate) }
    assertEquals(activity, activities.getActivity(activity.id))
    assertFailsWith<AppException.InvalidReference> {
        activities.save(activity.copy(id = ActivityId("invalid"), tags = setOf(sampleTag("Absent", TagGroup.TYPE, id = TagId("missing")))))
    }
    assertNull(activities.getActivity(ActivityId("invalid")))
    val minimal = activity.copy(
        id = ActivityId("minimal"), steps = emptyList(), variants = emptyList(), note = null,
        image = null, document = null, tags = emptySet(), equipment = emptySet(),
    )
    activities.save(minimal)
    assertEquals(minimal, activities.getActivity(minimal.id))
    val review = Review(ReviewId("review"), activity.id, Rating.GOOD, "Très bien")
    val favorite = Favorite(FavoriteId("favorite"), activity.id)
    reviews.save(review)
    favorites.addFavorite(favorite)
    assertEquals(review, reviews.getReview(review.id))
    assertEquals(listOf(review), reviews.observeReviews(activity.id).first())
    assertTrue(favorites.isFavorite(activity.id))
    assertEquals(listOf(favorite), favorites.observeFavorites().first())
    reviews.save(review)
    assertEquals(listOf(review), reviews.observeReviews(activity.id).first())
    assertFailsWith<AppException.Conflict> { favorites.addFavorite(favorite) }
    assertFailsWith<AppException.Conflict> { favorites.addFavorite(favorite.copy(id = FavoriteId("another"))) }
    assertFailsWith<AppException.Conflict> { favorites.addFavorite(favorite.copy(activityId = minimal.id)) }
    assertEquals(listOf(favorite), favorites.observeFavorites().first())
    assertFailsWith<AppException.InvalidReference> {
        reviews.save(review.copy(id = ReviewId("invalid"), activityId = ActivityId("missing")))
    }
    assertFailsWith<AppException.InvalidReference> {
        favorites.addFavorite(Favorite(FavoriteId("invalid"), ActivityId("missing")))
    }
    assertFailsWith<AppException.InvalidReference> {
        reviews.save(review.copy(activityId = ActivityId("missing")))
    }
    assertEquals(review, reviews.getReview(review.id))
    val changedReview = review.copy(rating = Rating.VERY_GOOD, comment = "Excellent")
    reviews.save(changedReview)
    assertEquals(changedReview, reviews.getReview(review.id))
    assertTrue(favorites.isFavorite(activity.id))
    reviews.deleteReview(review.id)
    favorites.deleteFavorite(favorite.id)
    assertFailsWith<AppException.NotFound> { reviews.deleteReview(review.id) }
    assertFailsWith<AppException.NotFound> { favorites.deleteFavorite(favorite.id) }
    reviews.save(review)
    val additionalReview = review.copy(id = ReviewId("additional"))
    reviews.save(additionalReview)
    assertEquals(2, reviews.observeReviews(activity.id).first().size)
    favorites.addFavorite(favorite)
    val updated = activity.copy(steps = listOf(Step("Remplacée")), variants = emptyList(), tags = emptySet(), equipment = emptySet())
    activities.save(updated)
    assertEquals(updated, activities.getActivity(activity.id))
    assertEquals(2, reviews.observeReviews(activity.id).first().size)
    assertTrue(favorites.isFavorite(activity.id))
    assertFalse(activities.isTagUsed(tag.id))
    assertFalse(activities.isEquipmentUsed(item.id))
    activities.deleteActivity(activity.id)
    assertNull(reviews.getReview(review.id))
    assertNull(reviews.getReview(additionalReview.id))
    assertTrue(reviews.observeReviews(activity.id).first().isEmpty())
    assertFalse(favorites.isFavorite(activity.id))
    assertTrue(favorites.observeFavorites().first().isEmpty())
    activities.deleteActivity(minimal.id)
    assertNotNull(tags.getTag(tag.id))
    assertNotNull(equipment.getEquipment(item.id))
    tags.deleteTag(tag.id)
    equipment.deleteEquipment(item.id)
}
