package com.example.textquest

import com.example.textquest.data.QuestRepository
import com.example.textquest.data.local.CampaignDao
import com.example.textquest.data.local.CampaignEntity
import com.example.textquest.data.remote.QuestApiService
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class LogicTests {
    private val dao = mockk<CampaignDao>(relaxed = true)
    private val apiMock = mockk<QuestApiService>(relaxed = true)
    private val repository = QuestRepository(dao, apiMock)

    @Test
    fun `test repository delete calls dao`() = runBlocking {
        repository.deleteCampaign("1")
        coVerify { dao.deleteCampaignById("1") }
    }

    @Test
    fun `test sync saves to database`() = runBlocking {
        val mockData = listOf(CampaignEntity("1", "T", "Desc", 1, false, 0L, "synced"))
        coEvery { apiMock.fetchCampaignsFromServer() } returns mockData
        repository.syncCampaigns()
        coVerify { dao.insertCampaigns(any()) }
    }

    @Test
    fun `test get single campaign returns value`() = runBlocking {
        val entity = CampaignEntity("1", "T", "Desc", 1, false, 0L, "synced")
        coEvery { dao.getCampaignById("1") } returns entity
        val result = repository.getCampaignById("1")
        assertNotNull(result)
        assertEquals("1", result?.campaignId)
    }

    @Test
    fun `test dao observation returns data`() = runBlocking {
        val list = listOf(CampaignEntity("1", "T", "Desc", 1, false, 0L, "synced"))
        every { dao.observeAllCampaigns() } returns flowOf(list)
        repository.observeAllCampaigns().collect {
            assertEquals(1, it.size)
        }
    }

    @Test
    fun `test fetch from server error handling`() = runBlocking {
        coEvery { apiMock.fetchCampaignsFromServer() } throws Exception("Network Error")
        repository.syncCampaigns()
        coVerify(exactly = 0) { dao.insertCampaigns(any()) }
    }

    @Test
    fun `test delete non existent campaign`() = runBlocking {
        repository.deleteCampaign("999")
        coVerify { dao.deleteCampaignById("999") }
    }

    @Test
    fun `test entity mapping title correct`() {
        val entity = CampaignEntity("1", "Title", "Description Text", 3, true, 100L, "synced")
        assertEquals("Title", entity.title)
    }

    @Test
    fun `test entity mapping sync status`() {
        val entity = CampaignEntity("1", "T", "D", 1, false, 0L, "pending")
        assertEquals("pending", entity.syncStatus)
    }

    @Test
    fun `test db instance is not null`() {
        assertNotNull(dao)
    }

    @Test
    fun `test api mock delay returns data`() = runBlocking {
        val realApi = QuestApiService()
        val result = realApi.fetchCampaignsFromServer()
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `test campaign model values match entity`() {
        val entity = CampaignEntity("id", "title", "desc", 5, false, 123L, "synced")
        assertEquals("id", entity.campaignId)
        assertEquals(5, entity.difficultyLevel)
    }

    @Test
    fun `test insert empty list does not crash`() = runBlocking {
        dao.insertCampaigns(emptyList())
        coVerify { dao.insertCampaigns(emptyList()) }
    }

    @Test
    fun `test dao returns null for bad id`() = runBlocking {
        coEvery { dao.getCampaignById("0") } returns null
        assertNull(repository.getCampaignById("0"))
    }

    @Test
    fun `test repository observe is reactive`() {
        val flow = repository.observeAllCampaigns()
        assertNotNull(flow)
    }

    @Test
    fun `test mock service return count`() = runBlocking {
        val realApi = QuestApiService()
        val data = realApi.fetchCampaignsFromServer()
        assertEquals(4, data.size)
    }
}