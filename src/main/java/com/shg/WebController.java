package com.shg;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web controller that maps URL paths to Thymeleaf HTML templates.
 */
@Controller
public class WebController {

    @GetMapping({"/", "/login"})
    public String index() {
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    // Finance module
    @GetMapping("/finance/transactions")
    public String transactions() {
        return "finance/transactions";
    }

    @GetMapping("/finance/reports")
    public String reports() {
        return "finance/reports";
    }

    @GetMapping("/finance/analytics")
    public String analytics() {
        return "finance/analytics";
    }

    // Advisory module
    @GetMapping("/advisory/investments")
    public String investments() {
        return "advisory/investments";
    }

    @GetMapping("/advisory/schemes")
    public String schemes() {
        return "advisory/schemes";
    }

    @GetMapping("/advisory/recommendations")
    public String recommendations() {
        return "advisory/recommendations";
    }

    @GetMapping("/advisory/discussion")
    public String advisoryDiscussion() {
        return "advisory/discussion";
    }

    // Discussion module
    @GetMapping("/discussion/list")
    public String discussionList() {
        return "discussion/list";
    }

    @GetMapping("/discussion/detail")
    public String discussionDetail() {
        return "discussion/detail";
    }

    // Admin module
    @GetMapping("/admin/brokers")
    public String brokers() {
        return "admin/brokers";
    }

    @GetMapping("/admin/statistics")
    public String statistics() {
        return "admin/statistics";
    }

    @GetMapping("/admin/settings")
    public String settings() {
        return "admin/settings";
    }
}
