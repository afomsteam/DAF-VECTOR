package com.afomsteam.enlistedplanner.logic

import com.afomsteam.enlistedplanner.data.Mga

data class AlqGuide(val name:String, val definition:String)
data class HelpRoute(val category:String, val need:String, val agencies:List<String>)
data class ProfessionalRef(val area:String, val reference:String, val note:String="", val url:String="")

object PlannerGuidance {
    fun alqs(mga: Mga): List<AlqGuide> = when(mga) {
        Mga.EXECUTING_MISSION -> listOf(
            AlqGuide("Job Proficiency","Demonstrates knowledge and professional skill in assigned duties, achieving positive results and impact in support of the mission."),
            AlqGuide("Initiative","Assesses and takes independent or directed action to complete a task or mission that influences the mission or organization."),
            AlqGuide("Adaptability","Adjusts to changing conditions, to include plans, information, processes, requirements and obstacles in accomplishing the mission.")
        )
        Mga.LEADING_PEOPLE -> listOf(
            AlqGuide("Inclusion & Teamwork","Collaborates effectively with others to achieve an inclusive climate in pursuit of a common goal or to complete a task or mission."),
            AlqGuide("Emotional Intelligence","Exercises self-awareness, manages their own emotions effectively, demonstrates an understanding of others' emotions, and appropriately manages relationships."),
            AlqGuide("Communication","Articulates information in a clear and timely manner, both verbally and non-verbally, through active listening and messaging tailored to the appropriate audience.")
        )
        Mga.MANAGING_RESOURCES -> listOf(
            AlqGuide("Stewardship","Demonstrates responsible management of assigned resources, which may include time, equipment, people, funds and/or facilities."),
            AlqGuide("Accountability","Takes responsibility for the actions and behaviors of self and/or team; demonstrates reliability and transparency.")
        )
        Mga.IMPROVING_UNIT -> listOf(
            AlqGuide("Decision Making","Makes well-informed, effective and timely decisions under one's control that weigh constraints, risks, and benefits."),
            AlqGuide("Innovation","Thinks creatively about different ways to solve problems, implements improvements and demonstrates calculated risk-taking.")
        )
    }

    fun mileFocus(mga: Mga): List<String> = when(mga) {
        Mga.EXECUTING_MISSION -> listOf("Primary mission","AEF readiness","Mission Assurance Command & Control","Perceived threats & hazards to the mission","Operational mechanisms in place & practiced","Security Classification Guides / MOUs / Business Rules")
        Mga.LEADING_PEOPLE -> listOf("Communication","Discipline","Training","Professional and personal development of Airmen","Quality of life engagement")
        Mga.MANAGING_RESOURCES -> listOf("Manpower","Funds","Facilities","Guidance","Airmen's time")
        Mga.IMPROVING_UNIT -> listOf("Strategic alignment","Process operations","Robust self-assessment program","Data-driven decision","CAPs and progress from previous inspections/audits/reports")
    }

    val swotPrompts = mapOf(
        "Strengths" to listOf("What are your assets (not equipment)?","Which asset is strongest?","What makes you better than similar sections?","Do you have a strong customer base?","What is unique about your section?","How skilled are your technicians?","What things are you praised for?","What are your advantages over other sections?"),
        "Weaknesses" to listOf("What areas need improvement?","What things should you avoid?","Where do other sections have an advantage?","Are you lacking knowledge?","Are your personnel not skilled enough?","Do you have enough resources to start the project?"),
        "Opportunities" to listOf("What external changes will bring opportunities?","What are the current ongoing trends?","Will these trends affect you positively?","What items can you provide for customers that you are not already providing?"),
        "Threats" to listOf("What are the negative aspects in your environment/workcenter?","What obstacles are you facing in the current mission?","Are you following all published guidelines and directives?","Are members satisfied with their quality of life?","Do you see members separating from the Air Force?")
    )

    val helpingRoutes = listOf(
        HelpRoute("Family","Change in Schools", listOf("Military OneSource","Chaplain","Military & Family Life Consultant (MFLC)","Family Advocacy","Airman & Family Readiness","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Family","Deployments", listOf("Military OneSource","Chaplain","MFLC","Mental Health","Airman & Family Readiness","Legal Office","Red Cross","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Family","Divorce / Separation / ERD", listOf("Military OneSource","Chaplain","MFLC","Mental Health","Family Advocacy","Legal Office","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Family","Getting Married", listOf("Military OneSource","Chaplain","MFLC","Airman & Family Readiness","Legal Office","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Family","Loss of Family / Friend", listOf("Military OneSource","Chaplain","MFLC","Mental Health","Airman & Family Readiness","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Family","New Parent", listOf("Military OneSource","Chaplain","MFLC","Family Advocacy","Airman & Family Readiness","Red Cross","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Family","PCS", listOf("Military OneSource","Chaplain","MFLC","Airman & Family Readiness","Health Promotion","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Family","Raising Children", listOf("Military OneSource","Chaplain","MFLC","Family Advocacy","Airman & Family Readiness","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Family","Relationship Challenges", listOf("Military OneSource","Chaplain","MFLC","Mental Health","Family Advocacy","Airman & Family Readiness","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Family","Single Parent", listOf("Military OneSource","Chaplain","MFLC","Family Advocacy","Airman & Family Readiness","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Family","Spouse Finding Work", listOf("Military OneSource","Chaplain","MFLC","Airman & Family Readiness","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Family","Strengthening Marriage", listOf("Military OneSource","Chaplain","MFLC","Family Advocacy","Airman & Family Readiness","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Personal","Abuse / Trauma (Past)", listOf("Military OneSource","Chaplain","MFLC","Mental Health","Family Advocacy","Domestic Abuse Victim Advocate","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Personal","Alcohol / Substance Abuse", listOf("Military OneSource","Chaplain","MFLC","Mental Health","Family Advocacy","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Personal","Anger Management", listOf("Military OneSource","Chaplain","MFLC","Mental Health","Family Advocacy","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Personal","Anxiety", listOf("Military OneSource","Chaplain","MFLC","Mental Health","Family Advocacy","Airman & Family Readiness","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Personal","Dating Violence", listOf("Military OneSource","Chaplain","MFLC","Mental Health","Family Advocacy","Domestic Abuse Victim Advocate","SARC","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Personal","Discrimination", listOf("Military OneSource","Chaplain","MFLC","Mental Health","Equal Opportunity","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Personal","Domestic Violence", listOf("Chaplain","MFLC","Mental Health","Family Advocacy","Domestic Abuse Victim Advocate","SARC","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Personal","Health Concerns", listOf("Military OneSource","Chaplain","MFLC","Mental Health","Family Advocacy","Health Promotion","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Personal","Loneliness / Isolation", listOf("Military OneSource","Chaplain","MFLC","Mental Health","Family Advocacy","Airman & Family Readiness","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Personal","Low Self Esteem", listOf("Military OneSource","Chaplain","MFLC","Mental Health","Family Advocacy","Airman & Family Readiness","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Personal","Mediation", listOf("Military OneSource","Chaplain","MFLC","Equal Opportunity","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Personal","Sexual Assault / Rape", listOf("Military OneSource","Chaplain","MFLC","Mental Health","Family Advocacy","Airman & Family Readiness","Legal Office","Domestic Abuse Victim Advocate","SARC","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Personal","Sexual Harassment", listOf("Military OneSource","Chaplain","MFLC","Mental Health","Family Advocacy","Equal Opportunity","SARC","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Personal","Sleep Difficulties", listOf("Military OneSource","Chaplain","MFLC","Mental Health","Family Advocacy","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Personal","Stress", listOf("Military OneSource","Chaplain","MFLC","Mental Health","Family Advocacy","Airman & Family Readiness","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Personal","Suicide Ideation / Prevention", listOf("Military OneSource","Chaplain","MFLC","Mental Health","Family Advocacy","Red Cross","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Personal","Weight / Nutrition", listOf("Military OneSource","Chaplain","Mental Health","Health Promotion","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Finance","Emergency Need", listOf("Military OneSource","Airman & Family Readiness","Red Cross","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Finance","Foreclosure", listOf("Military OneSource","Airman & Family Readiness","Legal Office","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Finance","Mortgages / Loans", listOf("Military OneSource","Airman & Family Readiness","Legal Office","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Finance","Retirement / Separation", listOf("Military OneSource","Chaplain","MFLC","Mental Health","Airman & Family Readiness","Health Promotion","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Legal","Emergency Need", listOf("Military OneSource","Chaplain","Mental Health","Legal Office","Red Cross","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Legal","Foreclosure", listOf("Military OneSource","Legal Office","Supervisor","First Sergeant / SEL","Commander")),
        HelpRoute("Legal","Mortgages / Loans", listOf("Military OneSource","Legal Office","Supervisor","First Sergeant / SEL","Commander"))
    )

    val professionalRefs = listOf(
        ProfessionalRef("Foundational","AFDP 1 (The Air Force)"), ProfessionalRef("Foundational","AFH 1 (Airman)"), ProfessionalRef("Foundational","AFI 1-1 (Air Force Standards)"), ProfessionalRef("Foundational","Blue Book / Brown Book / Purple Book"),
        ProfessionalRef("Recognition","DAFI 36-2502 Enlisted Airman Promotion & Demotion Programs"), ProfessionalRef("Recognition","DAFI 36-2803 Military Decorations & Awards Program"), ProfessionalRef("Recognition","DAFPD 36-25 Military Promotions & Demotions"),
        ProfessionalRef("Standards & Enforcement","AFI 36-2909 Professional Relationships & Conduct"), ProfessionalRef("Standards & Enforcement","DAFI 36-2903 Dress & Personal Appearance"), ProfessionalRef("Standards & Enforcement","DAFI 90-302 The Inspection System"),
        ProfessionalRef("Personnel Programs","AFI 36-2406 Officer & Enlisted Evaluation Systems"), ProfessionalRef("Personnel Programs","AFI 36-2606 Reenlistment & Extension"), ProfessionalRef("Personnel Programs","DAFI 36-2110 Total Force Assignments"), ProfessionalRef("Personnel Programs","DAFI 36-3003 Military Leave Program"),
        ProfessionalRef("Military Customs / Protocol","AFI 34-1201 Protocol"), ProfessionalRef("Military Customs / Protocol","BHM Base Honor Guard Manual"), ProfessionalRef("Military Customs / Protocol","DAFPAM 34-1203 Drill & Ceremonies"),
        ProfessionalRef("Communication / Social Media","Air Force Social Media Guide"), ProfessionalRef("Communication / Social Media","DAFH 33-337 The Tongue & Quill"),
        ProfessionalRef("Housing","AFI 32-6000 Housing Management"), ProfessionalRef("Medical","AFI 36-3212 Physical Evaluation for Retention, Retirement & Separation"), ProfessionalRef("Medical","AFI 48-133 Duty Limiting Conditions"),
        ProfessionalRef("Casualty & Death","Military Commander & The Law"), ProfessionalRef("Casualty & Death","SGLI / TSGLI"),
        ProfessionalRef("Miscellaneous","AFI 31-218 Motor Vehicle Traffic Supervision"), ProfessionalRef("Miscellaneous","AFI 34-144 Child & Youth Programs"), ProfessionalRef("Miscellaneous","AFI 34-223 Private Organizations Program"), ProfessionalRef("Miscellaneous","DAFI 36-3101 Fundraising"),
        ProfessionalRef("Useful Link","Air Force e-Publishing",url="https://www.e-publishing.af.mil/"), ProfessionalRef("Useful Link","Air Force Doctrine",url="https://www.doctrine.af.mil/"), ProfessionalRef("Useful Link","Air Force Aid Society",url="https://www.afas.org/"), ProfessionalRef("Useful Link","Military OneSource",url="https://www.militaryonesource.mil/"), ProfessionalRef("Useful Link","myPay",url="https://mypay.dfas.mil/"), ProfessionalRef("Useful Link","Thrift Savings Plan",url="https://www.tsp.gov/"), ProfessionalRef("Useful Link","American Red Cross",url="https://www.redcross.org/")
    )
}
