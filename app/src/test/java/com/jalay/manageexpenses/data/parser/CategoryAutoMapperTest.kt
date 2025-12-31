package com.jalay.manageexpenses.data.parser

import com.jalay.manageexpenses.domain.model.CategoryMapping
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CategoryAutoMapperTest {

    private lateinit var categoryAutoMapper: CategoryAutoMapper

    private val testMappings = listOf(
        CategoryMapping(keyword = "amazon", category = "Shopping", icon = "shopping_bag"),
        CategoryMapping(keyword = "flipkart", category = "Shopping", icon = "shopping_bag"),
        CategoryMapping(keyword = "zomato", category = "Food & Dining", icon = "restaurant"),
        CategoryMapping(keyword = "swiggy", category = "Food & Dining", icon = "restaurant"),
        CategoryMapping(keyword = "uber", category = "Transport", icon = "directions_car"),
        CategoryMapping(keyword = "ola", category = "Transport", icon = "directions_car"),
        CategoryMapping(keyword = "netflix", category = "Entertainment", icon = "movie"),
        CategoryMapping(keyword = "electricity", category = "Utilities", icon = "bolt"),
        CategoryMapping(keyword = "bank", category = "Transfers", icon = "account_balance")
    )

    @Before
    fun setup() {
        categoryAutoMapper = CategoryAutoMapper(
            getMappings = { testMappings }
        )
    }

    @Test
    fun `categorize Amazon recipient should return Shopping`() = runTest {
        val result = categoryAutoMapper.categorize("Amazon Pay", "somebank")
        assertEquals("Shopping", result)
    }

    @Test
    fun `categorize Flipkart recipient should return Shopping`() = runTest {
        val result = categoryAutoMapper.categorize("Flipkart Seller", "somebank")
        assertEquals("Shopping", result)
    }

    @Test
    fun `categorize Zomato recipient should return Food & Dining`() = runTest {
        val result = categoryAutoMapper.categorize("Zomato", "somebank")
        assertEquals("Food & Dining", result)
    }

    @Test
    fun `categorize Swiggy recipient should return Food & Dining`() = runTest {
        val result = categoryAutoMapper.categorize("SWIGGY INDIA", "somebank")
        assertEquals("Food & Dining", result)
    }

    @Test
    fun `categorize Uber recipient should return Transport`() = runTest {
        val result = categoryAutoMapper.categorize("UBER INDIA", "somebank")
        assertEquals("Transport", result)
    }

    @Test
    fun `categorize Netflix recipient should return Entertainment`() = runTest {
        val result = categoryAutoMapper.categorize("Netflix Subscription", "somebank")
        assertEquals("Entertainment", result)
    }

    @Test
    fun `categorize unknown recipient with bank sender should return Transfers`() = runTest {
        val result = categoryAutoMapper.categorize("John Doe", "HDFC Bank")
        assertEquals("Transfers", result)
    }

    @Test
    fun `categorize unknown recipient with wallet sender should return Transfers`() = runTest {
        val result = categoryAutoMapper.categorize("John Doe", "Paytm Wallet")
        assertEquals("Transfers", result)
    }

    @Test
    fun `categorize unknown recipient with unknown sender should return Other`() = runTest {
        val result = categoryAutoMapper.categorize("Random Person", "unknown_sender")
        assertEquals("Other", result)
    }

    @Test
    fun `categorize should be case insensitive`() = runTest {
        val result1 = categoryAutoMapper.categorize("AMAZON PAY", "sender")
        val result2 = categoryAutoMapper.categorize("amazon pay", "sender")
        val result3 = categoryAutoMapper.categorize("Amazon Pay", "sender")

        assertEquals("Shopping", result1)
        assertEquals("Shopping", result2)
        assertEquals("Shopping", result3)
    }

    @Test
    fun `categorize should match partial recipient names`() = runTest {
        val result = categoryAutoMapper.categorize("Amazon Seller Services Pvt Ltd", "sender")
        assertEquals("Shopping", result)
    }

    @Test
    fun `getDefaultMappings should return non-empty list`() {
        val mapper = CategoryAutoMapper { emptyList() }
        val defaults = mapper.getDefaultMappings()
        
        assertTrue(defaults.isNotEmpty())
        assertTrue(defaults.any { it.keyword == "amazon" })
        assertTrue(defaults.any { it.keyword == "zomato" })
        assertTrue(defaults.any { it.keyword == "uber" })
    }
}
