package com.shg;

import com.shg.model.Comment;
import com.shg.model.Discussion;
import com.shg.model.GovernmentScheme;
import com.shg.model.InvestmentPlan;
import com.shg.model.Recommendation;
import com.shg.model.SHGGroup;
import com.shg.model.SHGMember;
import com.shg.model.Transaction;
import com.shg.repository.CommentRepository;
import com.shg.repository.DiscussionRepository;
import com.shg.repository.GovernmentSchemeRepository;
import com.shg.repository.InvestmentPlanRepository;
import com.shg.repository.RecommendationRepository;
import com.shg.repository.SHGGroupRepository;
import com.shg.repository.SHGMemberRepository;
import com.shg.repository.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DemoDataLoader {

    @Bean
    CommandLineRunner seedData(SHGGroupRepository groupRepository,
                               SHGMemberRepository memberRepository,
                               TransactionRepository transactionRepository,
                               InvestmentPlanRepository investmentPlanRepository,
                               GovernmentSchemeRepository governmentSchemeRepository,
                               RecommendationRepository recommendationRepository,
                               DiscussionRepository discussionRepository,
                               CommentRepository commentRepository) {
        return args -> {
            if (groupRepository.count() > 0) {
                return;
            }

            SHGGroup group = new SHGGroup("Shakti Mahila Sangha",
                    "Women-led SHG focused on savings, emergency lending, and small business growth.",
                    "Mysuru",
                    500.0);
            group.setStatus("ACTIVE");
            group.setTotalBalance(14600.0);
            group = groupRepository.save(group);

            SHGMember president = createMember("president.priya", "password123", "Priya Sharma", "President",
                    "priya@shg.local", "9876500001", group, "ACTIVE");
            SHGMember treasurer = createMember("treasurer.lakshmi", "password123", "Lakshmi Devi", "Treasurer",
                    "lakshmi@shg.local", "9876500002", group, "ACTIVE");
            SHGMember secretary = createMember("secretary.meena", "password123", "Meena Patel", "Secretary",
                    "meena@shg.local", "9876500003", group, "ACTIVE");
            SHGMember accountant = createMember("accountant.neha", "password123", "Neha Singh", "Accountant",
                    "neha@shg.local", "9876500009", group, "ACTIVE");
            SHGMember memberOne = createMember("member.anita", "password123", "Anita Rao", "Member",
                    "anita@shg.local", "9876500004", group, "ACTIVE");
            SHGMember memberTwo = createMember("member.savita", "password123", "Savita Kumari", "Member",
                    "savita@shg.local", "9876500005", group, "ACTIVE");
            SHGMember admin = createMember("admin.root", "admin123", "System Admin", "Admin",
                    "admin@shg.local", "9876500006", group, "APPROVED");
            SHGMember broker = createMember("broker.raj", "broker123", "Raj Kumar", "Broker",
                    "raj@rk-investments.local", "9876500007", group, "PENDING");
            SHGMember governmentOfficer = createMember("government.officer", "gov123", "Government Officer", "Government Officer",
                    "govt@shg.local", "9876500008", group, "ACTIVE");

            memberRepository.save(president);
            memberRepository.save(treasurer);
            memberRepository.save(secretary);
            memberRepository.save(accountant);
            memberRepository.save(memberOne);
            memberRepository.save(memberTwo);
            memberRepository.save(admin);
            memberRepository.save(broker);
            memberRepository.save(governmentOfficer);

            transactionRepository.save(createTransaction("SAVINGS", 5000, "Monthly savings", "Priya Sharma", group, president, LocalDateTime.now().minusMonths(2).withDayOfMonth(3)));
            transactionRepository.save(createTransaction("LOAN", 10000, "Business loan for tailoring unit", "Lakshmi Devi", group, president, LocalDateTime.now().minusMonths(2).withDayOfMonth(8)));
            transactionRepository.save(createTransaction("EXPENSE", 1200, "Meeting expenses", "Lakshmi Devi", group, treasurer, LocalDateTime.now().minusMonths(2).withDayOfMonth(12)));
            transactionRepository.save(createTransaction("SAVINGS", 4500, "Monthly savings", "Anita Rao", group, memberOne, LocalDateTime.now().minusMonths(1).withDayOfMonth(5)));
            transactionRepository.save(createTransaction("LOAN", 8000, "Education support loan", "Meena Patel", group, secretary, LocalDateTime.now().minusMonths(1).withDayOfMonth(18)));
            transactionRepository.save(createTransaction("SAVINGS", 5200, "Monthly savings", "Savita Kumari", group, memberTwo, LocalDateTime.now().withDayOfMonth(4)));
            transactionRepository.save(createTransaction("EXPENSE", 900, "Stationery and records", "Lakshmi Devi", group, treasurer, LocalDateTime.now().withDayOfMonth(9)));

            president.setSavingsAmount(5000.0);
            president.setLoanAmount(10000.0);
            secretary.setLoanAmount(8000.0);
            memberOne.setSavingsAmount(4500.0);
            memberTwo.setSavingsAmount(5200.0);
            memberRepository.save(president);
            memberRepository.save(secretary);
            memberRepository.save(memberOne);
            memberRepository.save(memberTwo);

            InvestmentPlan fdPlan = new InvestmentPlan("SBI Fixed Deposit", 1000.0, 8.5, "Low", 12, "State Bank of India");
            fdPlan.setDescription("Guaranteed low-risk return for surplus SHG savings.");
            fdPlan.setStatus("APPROVED");
            investmentPlanRepository.save(fdPlan);

            InvestmentPlan microFinancePlan = new InvestmentPlan("Micro Enterprise Growth Plan", 5000.0, 11.8, "Medium", 24, "RK Investments");
            microFinancePlan.setDescription("Structured investment plan for SHGs willing to take moderate risk.");
            microFinancePlan.setStatus("APPROVED");
            investmentPlanRepository.save(microFinancePlan);

            GovernmentScheme nrlm = new GovernmentScheme("DAY-NRLM", 250000.0, 7.0, 36, "Ministry of Rural Development");
            nrlm.setDescription("Subsidised credit support and capacity building for rural SHGs.");
            nrlm.setEligibility("Rural SHGs with active savings and meeting records.");
            governmentSchemeRepository.save(nrlm);

            GovernmentScheme janDhan = new GovernmentScheme("PM Jan Dhan Yojana", 10000.0, 0.0, 12, "Ministry of Finance");
            janDhan.setDescription("Basic banking access for SHG members and households.");
            janDhan.setEligibility("All eligible Indian citizens.");
            governmentSchemeRepository.save(janDhan);

            GovernmentScheme mudra = new GovernmentScheme("PM Mudra Yojana", 1000000.0, 9.0, 36, "Ministry of Finance");
            mudra.setDescription("Collateral-free loans for small SHG businesses and entrepreneurs.");
            mudra.setEligibility("SHG members with viable micro-enterprise activities.");
            governmentSchemeRepository.save(mudra);

            GovernmentScheme surakshaBima = new GovernmentScheme("PM Suraksha Bima Yojana", 0.0, 0.0, 12, "Ministry of Finance");
            surakshaBima.setDescription("Affordable accident insurance coverage for SHG members.");
            surakshaBima.setEligibility("Aged 18–70 with a linked savings account.");
            governmentSchemeRepository.save(surakshaBima);

            GovernmentScheme atalPension = new GovernmentScheme("Atal Pension Yojana", 0.0, 0.0, 240, "Ministry of Finance");
            atalPension.setDescription("Pension support for informal sector workers and SHG members.");
            atalPension.setEligibility("Members aged 18–40 with a savings account and Aadhaar linkage.");
            governmentSchemeRepository.save(atalPension);

            Recommendation recommendationOne = new Recommendation("Increase Monthly Savings", "Savings", "High", 12000.0, "Recommendation Engine");
            recommendationOne.setDescription("Current balance growth is below the six-month projection. Raising monthly savings will improve resilience.");
            recommendationOne.setStatus("APPROVED");
            recommendationRepository.save(recommendationOne);

            Recommendation recommendationTwo = new Recommendation("Place Idle Funds in Fixed Deposit", "Investment", "Medium", 3500.0, "Recommendation Engine");
            recommendationTwo.setDescription("A portion of idle funds can earn stable returns through a low-risk fixed deposit.");
            recommendationTwo.setStatus("APPROVED");
            recommendationRepository.save(recommendationTwo);

            Discussion discussion = new Discussion("How should we increase monthly savings?",
                    "We want to increase the monthly member contribution from Rs. 500 to Rs. 700 without burdening members. Please share practical ideas.",
                    "Priya Sharma",
                    "Finance");
            discussion.setCreatedAt(LocalDateTime.now().minusDays(6));
            discussion.setUpdatedAt(LocalDateTime.now().minusDays(1));
            discussion = discussionRepository.save(discussion);

            Comment commentOne = new Comment("We can phase the increase over two months.", "Lakshmi Devi", discussion);
            commentOne.setCreatedAt(LocalDateTime.now().minusDays(5));
            commentRepository.save(commentOne);

            Comment commentTwo = new Comment("Members with active loans may need a separate transition plan.", "Meena Patel", discussion);
            commentTwo.setCreatedAt(LocalDateTime.now().minusDays(4));
            commentRepository.save(commentTwo);
        };
    }

    private SHGMember createMember(String username, String password, String fullName, String role,
                                   String email, String phone, SHGGroup group, String status) {
        SHGMember member = new SHGMember(username, password, fullName, role, group);
        member.setEmail(email);
        member.setPhoneNumber(phone);
        member.setStatus(status);
        member.setCreatedAt(LocalDateTime.now().minusMonths(3));
        member.setUpdatedAt(LocalDateTime.now().minusDays(1));
        member.setJoinedDate(LocalDateTime.now().minusMonths(3));
        return member;
    }

    private Transaction createTransaction(String type, double amount, String description, String recordedBy,
                                          SHGGroup group, SHGMember member, LocalDateTime timestamp) {
        Transaction transaction = new Transaction(type, amount, recordedBy, group);
        transaction.setDescription(description);
        transaction.setMember(member);
        transaction.setTransactionDate(timestamp);
        transaction.setCreatedAt(timestamp);
        return transaction;
    }
}
