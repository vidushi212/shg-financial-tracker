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

import java.util.Arrays;
import java.util.List;
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
            seedInvestmentPlans(investmentPlanRepository);

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

    private void seedInvestmentPlans(InvestmentPlanRepository investmentPlanRepository) {
        List<InvestmentPlanSeed> plans = Arrays.asList(
                new InvestmentPlanSeed("SBI Fixed Deposit",
                        "Guaranteed low-risk return for surplus SHG savings.",
                        1000.0, 8.5, "Low", 12, "State Bank of India"),
                new InvestmentPlanSeed("Micro Enterprise Growth Plan",
                        "Structured investment plan for SHGs willing to take moderate risk.",
                        5000.0, 11.8, "Medium", 24, "RK Investments"),
                new InvestmentPlanSeed("HDFC Recurring Deposit Plus",
                        "Monthly contribution plan with stable returns for disciplined SHG savings.",
                        500.0, 7.9, "Low", 18, "HDFC Bank"),
                new InvestmentPlanSeed("ICICI Women Prosperity Deposit",
                        "Capital protection focused deposit tailored for women-led groups.",
                        2000.0, 8.1, "Low", 15, "ICICI Bank"),
                new InvestmentPlanSeed("NABARD Rural Growth Fund",
                        "Diversified rural development fund with moderate long-term appreciation.",
                        7500.0, 10.4, "Medium", 36, "NABARD"),
                new InvestmentPlanSeed("Post Office Time Deposit",
                        "Government-backed fixed return option for secure reserve planning.",
                        1000.0, 7.7, "Low", 24, "India Post"),
                new InvestmentPlanSeed("Axis Balanced Advantage Plan",
                        "Balanced allocation plan combining equity growth with debt stability.",
                        3000.0, 10.9, "Medium", 30, "Axis Mutual Fund"),
                new InvestmentPlanSeed("SBI Gold Savings Basket",
                        "Gold-linked savings product for inflation-aware long-term accumulation.",
                        2500.0, 9.2, "Medium", 18, "SBI Mutual Fund"),
                new InvestmentPlanSeed("Kotak Small Business Bond",
                        "Fixed-income bond product supporting predictable medium-term cash growth.",
                        4000.0, 9.8, "Medium", 20, "Kotak Securities"),
                new InvestmentPlanSeed("Mahila Udyam Equity Link",
                        "Higher-growth investment basket suited to SHGs building enterprise reserves.",
                        10000.0, 13.1, "High", 36, "Mahila Capital Advisors"),
                new InvestmentPlanSeed("Reliance Rural Infra Notes",
                        "Infrastructure-linked notes offering elevated return potential with higher risk.",
                        8000.0, 12.4, "High", 30, "Reliance Securities")
        );

        plans.forEach(plan -> upsertInvestmentPlan(investmentPlanRepository, plan));
    }

    private void upsertInvestmentPlan(InvestmentPlanRepository investmentPlanRepository, InvestmentPlanSeed seed) {
        InvestmentPlan plan = investmentPlanRepository.findByPlanName(seed.planName())
                .orElseGet(() -> new InvestmentPlan(
                        seed.planName(),
                        seed.minimumAmount(),
                        seed.expectedReturn(),
                        seed.riskLevel(),
                        seed.durationMonths(),
                        seed.brokerName()
                ));

        plan.setDescription(seed.description());
        plan.setMinimumAmount(seed.minimumAmount());
        plan.setExpectedReturn(seed.expectedReturn());
        plan.setRiskLevel(seed.riskLevel());
        plan.setDurationMonths(seed.durationMonths());
        plan.setBrokerName(seed.brokerName());
        plan.setStatus("APPROVED");
        investmentPlanRepository.save(plan);
    }

    private static final class InvestmentPlanSeed {
        private final String planName;
        private final String description;
        private final Double minimumAmount;
        private final Double expectedReturn;
        private final String riskLevel;
        private final Integer durationMonths;
        private final String brokerName;

        private InvestmentPlanSeed(String planName,
                                   String description,
                                   Double minimumAmount,
                                   Double expectedReturn,
                                   String riskLevel,
                                   Integer durationMonths,
                                   String brokerName) {
            this.planName = planName;
            this.description = description;
            this.minimumAmount = minimumAmount;
            this.expectedReturn = expectedReturn;
            this.riskLevel = riskLevel;
            this.durationMonths = durationMonths;
            this.brokerName = brokerName;
        }

        private String planName() {
            return planName;
        }

        private String description() {
            return description;
        }

        private Double minimumAmount() {
            return minimumAmount;
        }

        private Double expectedReturn() {
            return expectedReturn;
        }

        private String riskLevel() {
            return riskLevel;
        }

        private Integer durationMonths() {
            return durationMonths;
        }

        private String brokerName() {
            return brokerName;
        }
    }
}
