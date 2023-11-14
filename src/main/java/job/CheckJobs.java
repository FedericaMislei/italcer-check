package job;

import freemarker.template.Configuration;
import freemarker.template.Template;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.inject.Inject;
import job.dao.ControlloDAO;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.scheduler.Scheduled;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class CheckJobs {
    @Inject
    ControlloDAO controlloDAO;
    @Inject
    Mailer mailer;
    @ConfigProperty(name = "custom.mail.1.type ")
    String tipo1;

    @ConfigProperty(name = "custom.mail.2.type ")
    String tipo2;
    @ConfigProperty(name = "custom.mail.3.type ")
    String tipo3;

    @Scheduled(cron = "0 15 8 ? * MON,TUE,WED,THU,FRI ", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void start() {
        send("federica.mislei@quix,it",tipo1);
    }

    private void send(String email, String tipocontrollo) {
        if(tipocontrollo.equals("controllo1")){
            String resultmysql=controlloDAO.countMySql();
            log.info("resultmysql: " + resultmysql);
            String resultas400=controlloDAO.countAS400();
            log.info("resultas400: " + resultas400);
            int mysql=Integer.parseInt(resultmysql);
            int as400=Integer.parseInt(resultas400);
            List<Map<String,Object>> lista= new ArrayList<>();
            Map<String,Object> mappa=new HashMap<>();
            mappa.put("resultmysql",resultmysql);
            mappa.put("resultas400",resultas400);
            lista.add(mappa);
            inviaEmail(email,lista,"","haoran.chen@quix.it");
        }
    }
    private void inviaEmail(String dest, List<Map<String, Object>> list, String email, String cc) {
        log.info("inizio invio email");
        StringWriter wtitle = new StringWriter();
        StringWriter wbody = new StringWriter();
        try {
            Configuration cfg = new Configuration();
            // Where do we load the templates from:
            cfg.setClassForTemplateLoading(CheckJobs.class, "templates");
            // Some other recommended settings:
            cfg.setDefaultEncoding("UTF-8");
            cfg.setLocale(Locale.ITALY);
            Map<String, Object> vars = new HashMap<>();
            vars.put("email", email);
            vars.put("list", list);
            // title
            InputStream in = this.getClass().getResourceAsStream("/templates/title.ftl");
            String templateStrTitle = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            Template template = new Template("title", new StringReader(templateStrTitle), cfg);
            template.process(vars, wtitle);
            wtitle.close();
            String object = wtitle.toString();
            // title
            InputStream in2 = this.getClass().getResourceAsStream("/templates/body.ftl");
            String templateStrBody = new String(in2.readAllBytes(), StandardCharsets.UTF_8);
            template = new Template("body", new StringReader(templateStrBody), cfg);
            template.process(vars, wbody);
            wbody.close();
            String text = wbody.toString();
            // TODO
            log.debug("Send email to = " + dest);
            log.debug("Object = " + object);
            log.debug("Text = " + text);
            if (dest.toLowerCase().contains("support")) {
                return;
            }
            sendEmail(object, text, dest, cc); // TODO destinatario
            log.info("fine invio email");
        } catch (Exception e) {
            log.error("Error on send email", e);
        } finally {
            try {
                wtitle.close();
            } catch (IOException e) {
            }
            try {
                wbody.close();
            } catch (IOException e) {
            }
        }
    }

    private void sendEmail(String subject, String body, String emailTo, String cc) throws Exception {
        log.debug("sendEmail");
        try {
//            subject= emailTo+" "+ cc+ " "+subject;
//            emailTo="federica.mislei@quix.it";
//            cc="luca.marcone@quix.it";
            //  if (emailTo.equals("giorgia.baldini@quix.it") || emailTo.equals("mattia.bonacorsi@quix.it")) {
            Mail mail = Mail.withHtml(emailTo, subject, body);
            //copia nascosta
//                List<String> bcc = new ArrayList<>();
//                bcc.add("federica.mislei@quix.it");
//                bcc.add("luca.marcone@quix.it");
//                mail.setBcc(bcc);
            //copia conoscenza
            mail.addCc(cc);
            mailer.send(mail);
            //  }
        } catch (Exception e) {
            log.error("Errore durante l'invio della mail", e);
            throw e;
        }
    }
}
