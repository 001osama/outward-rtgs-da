package service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped  
public class ReportService {
    private IReportEngine birtEngine;

    private static final Logger logger = Logger.getLogger(ReportService.class.getName());

    // @PostConstruct
    // public void init() {
//        try {
////            EngineConfig config = new EngineConfig();
////          //  config.getAppContext().put("spring", this.getClass());
////            Platform.startup(config);
////            IReportEngineFactory engine = (IReportEngineFactory) Platform
////                    .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
////            IReportRunnable report = engine.
////            if(factory != null)
////            birtEngine = factory.createReportEngine(config);
//        } catch (BirtException e) {
////            logger.log(Level.SEVERE, "Failed to initialize BIRT engine", e);
//        }
    // }

    public  byte[]  generateReport(Map<String, Object> params) throws BirtException {

        System.out.println("inside birt service: generating report with params :"+ params);
//        EngineConfig config = new EngineConfig();
//        //  config.getAppContext().put("spring", this.getClass());
//        Platform.startup(config);
//        IReportEngineFactory factory = (IReportEngineFactory) Platform
//                .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
//        if(factory != null)
//            birtEngine = factory.createReportEngine(config);

        // String baseDir = System.getProperty("user.dir");
       // String reportDesignPath = baseDir + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + designFileName;
       // String outputPath = baseDir + File.separator + outputFileName;

        // System.setProperty("java.awt.headless", "true");
        EngineConfig engineConfig = new EngineConfig();
        engineConfig.setEngineHome("");
        Platform.startup(engineConfig);
        IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
        IReportEngine engine = factory.createReportEngine(engineConfig);
        IReportRunnable report = engine.openReportDesign("/home/bahl/Project/outward-rtgs-da/src/main/resources/report/rtgs_report.rptdesign");
        IRunAndRenderTask task = engine.createRunAndRenderTask(report);
        if (params!= null){
            task.setParameterValues(params);
        } 

        System.out.println("inside birt service: parameter set on task!!!!!");
        PDFRenderOption options = new PDFRenderOption();
        options.setOutputFormat("pdf");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        options.setOutputStream(outputStream);

        task.setRenderOption(options);

        task.run();
        task.close();

        byte[] reportContent =  outputStream.toByteArray();
        return reportContent;
//
//        String outputPath = "/home/bahl/Downloads/pich-report/src/main/resources/";
//
//        IReportRunnable design = birtEngine.openReportDesign("/home/bahl/Downloads/pich-report/src/main/resources/new_report.rptdesign");
//        IRunAndRenderTask task = birtEngine.createRunAndRenderTask(design);
//
//        PDFRenderOption pdfOptions = new PDFRenderOption();
//        String outputFilePath = outputPath  + File.separator + "my_report_" + System.currentTimeMillis() + ".pdf";
//        pdfOptions.setOutputFileName(outputFilePath);
//        pdfOptions.setOutputFormat("pdf");
//        task.setRenderOption(pdfOptions);
//
//        task.run();
//        task.close();
    }

    @PreDestroy
    public void shutdown() {
        if (birtEngine != null) {
            birtEngine.destroy();
            Platform.shutdown();
        }
    }
}