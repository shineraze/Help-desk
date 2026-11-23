package ru.example.helpdesk.repository;

import ru.example.helpdesk.model.AgentLoad;
import ru.example.helpdesk.model.CategoryTicketCount;

import java.util.List;
import java.util.Map;

public interface ReportRepository {

    Map<String, Long> countByStatus();

    List<CategoryTicketCount> countByCategory();

    List<AgentLoad> agentLoad();
}
