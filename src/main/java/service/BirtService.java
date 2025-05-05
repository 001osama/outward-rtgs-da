package service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import jakarta.enterprise.context.ApplicationScoped;
// import jakarta.annotation.PostConstruct;
// import jakarta.annotation.PreDestroy;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.*;

import dto.ReportFilterDto;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@ApplicationScoped
public class BirtService {
    private IReportEngine birtEngine;
    private static final Logger logger = Logger.getLogger(BirtService.class.getName());

    @PostConstruct
    public void init() {
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
    }

    public byte[] generateReport(ReportFilterDto reportFilter) throws BirtException {

        Map<String, Object> params = new HashMap<>();
        if (reportFilter != null) { // Add a null check
            params.put("branch", reportFilter.getBranchCode());
            // params.put("rtgsType", reportFilter.getRtgsType());
            params.put("transferType", reportFilter.getTransactionType());
            // params.put("customerNo", reportFilter.getCustomerNo());
            // params.put("accountNo", reportFilter.getAccountNo());
            params.put("status", reportFilter.getStatus());
        }


        if (Boolean.TRUE.equals(reportFilter.getIsFromDate())) {
            params.put("fromdate", reportFilter.getFromDate());
        }

        if (Boolean.TRUE.equals(reportFilter.getIsToDate())) {
            params.put("todate", reportFilter.getToDate());
        }


        EngineConfig engineConfig = new EngineConfig();
        engineConfig.setEngineHome("");
        Platform.startup(engineConfig);
        IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
        IReportEngine engine = factory.createReportEngine(engineConfig);

        IReportRunnable report = engine.openReportDesign("/home/bahl/Project/outward-rtgs-da/src/main/resources/rtgs_report.rptdesign");

        IRunAndRenderTask task = engine.createRunAndRenderTask(report);

        if (params!= null){
            task.setParameterValues(params);
        } 


        PDFRenderOption options = new PDFRenderOption();
        options.setOutputFormat("pdf");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        options.setOutputStream(outputStream);

        task.setRenderOption(options);

        task.run();
        task.close();

       byte[] reportContent =  outputStream.toByteArray();
       return reportContent;
    }

    @PreDestroy
    public void shutdown() {
        if (birtEngine != null) {
            birtEngine.destroy();
            Platform.shutdown();
        }
    }
}
