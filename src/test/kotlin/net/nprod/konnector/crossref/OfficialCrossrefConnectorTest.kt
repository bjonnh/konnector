package net.nprod.konnector.crossref

import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.Test

@KtorExperimentalAPI
internal class OfficialCrossrefConnectorTest {
    private var connector = CrossRefConnector(OfficialCrossRefAPI())

    @Test
    fun `basic search`() {
        val output: WorksResponse = connector.works(query = "bisson can invalid bioactives medicinal 10.1021/acs.jmedchem.5b01009")
        assert(output.message?.items?.get(0)?.DOI == "10.1021/acs.jmedchem.5b01009")
    }
}
/*
internal class MockAPI : CrossRefAPI {
    override fun call(url: String, parameters: Map<String, String>): IO<String> {
        return when (url) {
            "/works" ->
                when (parameters["queryTextArea.title"]) {
                    "Driver Fatigue Detection" -> IO { File(javaClass.getResource("/crossref-works.json").path).readText() }
                    else -> IO { "" }
                }
            "/works/10.0000/0000" -> IO { File(javaClass.getResource("/crossref-work.json").path).readText() }

            else -> IO { "" }
        }
    }

    internal class CrossRefConnectorTest {
        private lateinit var conn: CrossRefConnector
        @BeforeEach
        fun setUp() {
            conn = CrossRefConnector(MockAPI())
        }

        @AfterEach
        fun tearDown() {
        }

        @Test
        fun workFromDOI() {
            conn.workFromDOI("10.0000/0000").unsafeRunAsync { result ->
                result.fold({
                    throw it
                }, {
                    assertEquals("ok", it.status)
                    assertEquals("work", it.messageType)
                    assertEquals("1.0.0", it.messageVersion)
                    val message = it.message
                    if (message != null) {

                        with(message) {
                            assertTrue(
                                this.indexed == DateBlock(
                                    "2018-10-09T21:22:37Z",
                                    listOf(listOf(2018, 10, 9)), 1539120157357
                                )
                            )
                            assertEquals(105, this.referenceCount)
                            assertEquals(
                                "American Psychological Association (APA)",
                                it.message?.publisher
                            )
                            assertEquals("1", this.issue)
                            assertEquals(false, this.contentDomain?.crossmarkRestriction)
                            assertEquals(0, this.contentDomain?.domain?.size)
                            assertEquals(listOf("American Psychologist"), this.shortContainerTitle)
                            assertEquals("10.1037/0003-066x.59.1.29", this.DOI)
                            assertEquals("journal-article", this.type)
                            assertTrue(
                                this.created == DateBlock(
                                    "2004-01-21T14:31:19Z",
                                    listOf(listOf(2004, 1, 21)), 1074695479000
                                )
                            )
                            assertEquals("29-40", this.page)
                            assertEquals("Crossref", this.source)
                            assertEquals(80, this.isReferencedByCount)
                            assertEquals(listOf("How the Mind Hurts and Heals the Body."), this.title)
                            assertEquals("10.1037", this.prefix)
                            assertEquals("59", this.volume)
                            assertTrue(
                                Author(
                                    ORCID = null,
                                    authenticatedOrcid = null,
                                    given = "Oakley",
                                    family = "Ray",
                                    sequence = "first",
                                    affiliation = listOf()
                                ) == this.author?.get(0))
                        }

                    } else {
                        throw Error("No message!")
                    }

                    /*
                    var position: Int? = 0,

                    @Json(name = "funder") val funders: List<Funder>?,
                    @Json(name = "content-domain") val contentDomain: Domain?,
                    @Json(name = "short-container-title") val shortContainerTitle: List<String>?,
                    val abstract: String?,
                    val DOI: String,
                    val type: String?,
                    val created: DateBlock?,
                    val page: String?,
                    @Json(name = "update-policy") val updatePolicy: String?,
                    val source: String?,
                    @Json(name = "is-referenced-by-count") val isReferencedByCount: Int?,
                    val title: List<String>?,
                    val firstTitle: String? = title?.first(),
                    val prefix: String?,
                    val volume: String?,
                    val author: List<Author>?,
                    val member: String?,
                    @Json(name = "published-online") val publishedOnline: DateBlock?,
                    @Json(name = "published-print") val publishedPrint: DateBlock?,
                    val reference: List<Reference>?,
                    @Json(name = "container-title") val containerTitle: List<String>?,
                    val firstContainerTitle: String? = containerTitle?.first(),
                    @Json(name = "original-title") val originalTitle: List<String>?,
                    val language: String?,
                    val link: List<Link>?,
                    val deposited: DateBlock?,
                    val score: Double?,
                    val subtitle: List<String>?,
                    @Json(name = "short-title") val shortTitle: List<String>?,
                    val issued: DateBlock?,
                    @Json(name = "references-count") val referencesCount: Int?,
                    @Json(name = "journal-issue") val journalIssue: Issue?,
                    val URL: String?,
                    val relation: Relation?,
                    val ISSN: List<String>?,
                    @Json(name = "issn-type") val issnType: List<ISSN>?,
                    val assertion: List<Assertion>?

                     */
                })
            }
        }

        @Test
        fun works() {
            conn.works("Driver Fatigue Detection").unsafeRunAsync { result ->
                result.fold({
                    throw it
                }, {
                    assertEquals("ok", it.status)
                    assertEquals(20, it.message?.items?.size)
                    assertEquals("work-list", it.messageType)
                    assertEquals("1.0.0", it.messageVersion)
                    assertNotNull(it.message?.facets)
                })
            }
        }

        @Test
        fun failsWrongfile() {
            assertThrows(DecodingError::class.java) {
                conn.works("Something else").unsafeRunAsync { result ->
                    result.fold({
                        throw it
                    }, {
                        throw Error("Nope")
                    })
                }
            }
        }
    }
}*/
