package com.zabud.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import lombok.extern.slf4j.Slf4j;


@Configuration
@EnableScheduling
@Slf4j
public class ScheduledConfig implements SchedulingConfigurer {

  @Autowired
  Executor taskExecutor;

  @Value("${mock.default.credential.credentialUrl}")
  String urlCredencial;

  private final TaskDTO[] tasksArray;

  public ScheduledConfig() throws IOException {
    val objectMapper = new ObjectMapper();
    val tasksString = loadResource("tasks.json");
    tasksArray = objectMapper.readValue(tasksString, TaskDTO[].class);
  }

  @Override
  public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    Arrays.stream(tasksArray).forEach(task -> loadTask(taskRegistrar, task));
  }

  private void loadTask(ScheduledTaskRegistrar taskRegistrar, TaskDTO task) {
    CronTrigger cronTrigger = new CronTrigger(task.getCronexp());
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        @val
        java.util.stream.Stream<com.zabud.example.CredentialDTO> credentials;
        log.info("Ejecutar tarea: {}", task.getName());
        try {
          credentials = getCredentials().stream().filter(c -> task.getNits().contains(c.getNit()));
          credentials.forEach(c -> {
            proccessRequest(c, task);
          });
        } catch (IOException e) {
          e.printStackTrace();
          log.info("Error", task.getName(), task.getUrl());
        }
      }
    };
    taskRegistrar.addTriggerTask(runnable, cronTrigger);
  }

  protected void proccessRequest(CredentialDTO credential, TaskDTO task) {
    String url = task.getUrl().replace("{nit}", credential.getNit())
        .replace("{tokenPrimario}", credential.getToken())
        .replace("{tokenSecundario}", credential.getSecondaryToken())
        .replace("{regimen}", credential.getRegimen());
    try {
      excecuteRequest(url, String.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private List<CredentialDTO> getCredentials()
      throws JsonParseException, JsonMappingException, IOException {
    return Arrays.asList(this.excecuteRequest(urlCredencial, CredentialDTO[].class));
  }

  private <T> T excecuteRequest(String url, Class<T> type)
      throws JsonParseException, JsonMappingException, IOException {
    log.info("Ejecutar query a:{}", url);
    val objectMapper = new ObjectMapper();
    val restTemplate = new RestTemplate();
    val response = restTemplate.getForEntity(url, String.class);
    return objectMapper.readValue(response.getBody(), type);
  }

  private static String loadResource(String fileName) throws IOException {
    ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    try (InputStream is = classLoader.getResourceAsStream(fileName)) {
      if (is == null)
        return null;
      try (InputStreamReader isr = new InputStreamReader(is);
          BufferedReader reader = new BufferedReader(isr)) {
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
      }
    }
  }


}
