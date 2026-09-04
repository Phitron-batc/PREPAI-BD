package com.example.data.engine

import com.example.data.model.AiReviewQueueItem
import com.example.data.model.DifficultyLevel
import com.example.data.model.DocumentInfo
import com.example.data.model.DocumentStatus
import com.example.data.model.Question
import com.example.data.model.QuestionStatus
import com.example.data.model.ReviewStatus
import java.util.UUID

/**
 * Stage representation in the Document Ingestion & RAG Indexing Pipeline.
 */
enum class RagPipelineStage {
    UPLOAD,
    STORAGE,
    TEXT_EXTRACTION,
    OCR_PROCESSING,
    CHUNKING,
    METADATA_EXTRACTION,
    EMBEDDINGS_GENERATION,
    VECTOR_STORAGE,
    RETRIEVAL_READY
}

data class RetrievedRagChunk(
    val chunkId: String,
    val sourceDocumentTitle: String,
    val pageNumber: Int,
    val contentSnippet: String,
    val similarityScore: Float
)

data class RagQueryResponse(
    val query: String,
    val retrievedChunks: List<RetrievedRagChunk>,
    val assembledContext: String,
    val generatedAnswer: String,
    val isLiveModelUsed: Boolean
)

object RagKnowledgeEngine {

    val SUPPORTED_DOCUMENT_TYPES = listOf(
        "PDF Official Circular",
        "Official Syllabus / Outline",
        "Verified Subject Notes",
        "Licensed Educational Material",
        "PSC Question Archive"
    )

    /**
     * Simulates or executes the 9-stage Document Ingestion & RAG Indexing Pipeline.
     */
    fun processDocumentPipeline(
        title: String,
        documentType: String,
        category: String,
        pageCount: Int,
        sourceMetadata: String
    ): DocumentInfo {
        val estimatedChunks = pageCount * 4
        return DocumentInfo(
            id = "doc_${UUID.randomUUID().toString().take(8)}",
            title = title,
            documentType = documentType,
            category = category,
            fileSize = "${(pageCount * 0.35).coerceAtLeast(0.5).let { "%.1f".format(it) }} MB",
            pageCount = pageCount,
            chunkCount = estimatedChunks,
            uploadDate = "04 Sep 2026",
            processingStatus = DocumentStatus.READY,
            vectorStatus = "Indexed ($estimatedChunks vectors ready)",
            sourceMetadata = sourceMetadata,
            copyrightCleared = true
        )
    }

    /**
     * RAG Retrieval & Answer Assembly Pipeline:
     * User Question -> Query Processing -> Relevant Content Retrieval -> Context Assembly -> AI Model -> Answer
     */
    fun executeRagRetrieval(
        userQuery: String,
        indexedDocs: List<DocumentInfo>
    ): RagQueryResponse {
        val matchingDoc = indexedDocs.firstOrNull { doc ->
            userQuery.split(" ").any { word -> word.length > 3 && doc.title.contains(word, ignoreCase = true) }
        } ?: indexedDocs.firstOrNull()

        val sampleChunks = listOf(
            RetrievedRagChunk(
                chunkId = "chunk_${UUID.randomUUID().toString().take(6)}",
                sourceDocumentTitle = matchingDoc?.title ?: "46th BCS Preliminary Syllabus",
                pageNumber = 14,
                contentSnippet = "সংবিধানের অনুচ্ছেদ ২৭ (আইনের দৃষ্টিতে সমতা), ২৮ (ধর্ম প্রভৃতি কারণে বৈষম্য), এবং ২৯ (সরকারি নিয়োগ লাভে সুযোগের সমতা) এর বিধানাবলী প্রজাতন্ত্রের সকল নাগরিকের জন্য অলঙ্ঘনীয় অধিকার হিসেবে স্বীকৃত।",
                similarityScore = 0.89f
            ),
            RetrievedRagChunk(
                chunkId = "chunk_${UUID.randomUUID().toString().take(6)}",
                sourceDocumentTitle = matchingDoc?.title ?: "46th BCS Preliminary Syllabus",
                pageNumber = 22,
                contentSnippet = "বাংলাদেশ সরকারি কর্ম কমিশন (বিপিএসসি) সংবিধানের ১৩৭ অনুচ্ছেদ অনুযায়ী গঠিত একটি সাংবিধানিক প্রতিষ্ঠান। কমিশনের সভাপতি ও সদস্যদের নিয়োগ দেন রাষ্ট্রপতি।",
                similarityScore = 0.84f
            )
        )

        val assembledContext = sampleChunks.joinToString("\n\n") { "[Page ${it.pageNumber}]: ${it.contentSnippet}" }

        val answer = "উৎস নথি অনুসারে:\n" +
                "১. বাংলাদেশ সংবিধানের ১৩৭ অনুচ্ছেদ অনুযায়ী বাংলাদেশ সরকারি কর্ম কমিশন (BPSC) গঠিত হয় এবং রাষ্ট্রপতি এর সদস্য নিয়োগ প্রদান করেন।\n" +
                "২. অনুচ্ছেদ ২৭, ২৮ ও ২৯ সরকারি নিয়োগ ও মৌলিক অধিকারের ক্ষেত্রে সমতা নিশ্চিত করে।"

        return RagQueryResponse(
            query = userQuery,
            retrievedChunks = sampleChunks,
            assembledContext = assembledContext,
            generatedAnswer = answer,
            isLiveModelUsed = false
        )
    }

    /**
     * AI Question Generation Pipeline:
     * Document -> AI Analysis -> Question Draft -> Explanation -> Admin Review
     * (AI questions must NEVER automatically become public).
     */
    fun draftAiQuestionFromDocument(
        documentTitle: String,
        chapter: String,
        subject: String = "Bangladesh Affairs"
    ): AiReviewQueueItem {
        val generatedQuestion = Question(
            id = "ai_gen_${UUID.randomUUID().toString().take(8)}",
            examCategory = "BCS",
            subject = subject,
            topic = "বাংলাদেশ সংবিধান ও সাংবিধানিক প্রতিষ্ঠান",
            subtopic = "সরকারি কর্ম কমিশন (BPSC)",
            questionEn = "Under which article of the Constitution of Bangladesh is the Public Service Commission (BPSC) established?",
            questionBn = "গণপ্রজাতন্ত্রী বাংলাদেশের সংবিধানের কোন অনুচ্ছেদ অনুযায়ী সরকারি কর্ম কমিশন (BPSC) গঠিত হয়?",
            optionsEn = listOf("Article 135", "Article 137", "Article 140", "Article 142"),
            optionsBn = listOf("অনুচ্ছেদ ১৩৫", "অনুচ্ছেদ ১৩৭", "অনুচ্ছেদ ১৪০", "অনুচ্ছেদ ১৪২"),
            correctIndex = 1,
            explanationEn = "Article 137 of the Bangladesh Constitution authorizes the establishment of one or more Public Service Commissions.",
            explanationBn = "সংবিধানের ১৩৭ অনুচ্ছেদ অনুযায়ী এক বা একাধিক সরকারি কর্ম কমিশন গঠনের বিধান রাখা হয়েছে।",
            aiShortcut = "টেকনিক: পিএসসি ১৩৭, নির্বাচন কমিশন ১১৮, অ্যাটর্নি জেনারেল ৬৪।",
            difficulty = DifficultyLevel.MEDIUM,
            previousYearTag = "AI Draft from $documentTitle",
            isBookmarked = false,
            isFromAiReview = true,
            status = ReviewStatus.PENDING,
            questionStatus = QuestionStatus.PENDING_REVIEW,
            tags = listOf("AI_DRAFT", "CONSTITUTION", "BPSC"),
            sourceMetadata = "Drafted from verified document: $documentTitle"
        )

        return AiReviewQueueItem(
            id = "rev_queue_${UUID.randomUUID().toString().take(8)}",
            sourceDocument = documentTitle,
            chapter = chapter,
            question = generatedQuestion,
            status = ReviewStatus.PENDING,
            reviewerNote = "Awaiting Admin editorial and factual validation.",
            generatedDate = "04 Sep 2026, 11:15 AM"
        )
    }

    /**
     * Updates an AI review queue item with admin revision request notes.
     */
    fun requestRevision(
        queue: List<AiReviewQueueItem>,
        itemId: String,
        notes: String
    ): List<AiReviewQueueItem> {
        return queue.map { item ->
            if (item.id == itemId) {
                item.copy(
                    status = ReviewStatus.NEEDS_EDIT,
                    reviewerNote = notes
                )
            } else {
                item
            }
        }
    }

    /**
     * Executes RAG Retrieval with optional live Gemini synthesis.
     */
    suspend fun queryRagKnowledge(
        query: String,
        geminiService: com.example.data.ai.GeminiAiService?,
        indexedDocs: List<DocumentInfo> = emptyList()
    ): RagQueryResponse {
        val baseResponse = executeRagRetrieval(query, indexedDocs)
        if (geminiService != null && geminiService.isLiveApiConfigured()) {
            val prompt = "Context from verified syllabus documents:\n${baseResponse.assembledContext}\n\nCandidate Question: $query\nAnswer accurately with Bangladesh exam syllabus references."
            val liveAnswer = geminiService.askTutor(prompt, com.example.data.model.TutorMode.EXAM_MODE)
            return baseResponse.copy(
                generatedAnswer = liveAnswer,
                isLiveModelUsed = true
            )
        }
        return baseResponse
    }
}
