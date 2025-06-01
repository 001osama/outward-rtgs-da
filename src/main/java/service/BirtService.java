package service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import jakarta.enterprise.context.ApplicationScoped;
// import jakarta.annotation.PostConstruct;
// import jakarta.annotation.PreDestroy;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.*;

import dto.AccountNumberDto;
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

    public byte[] generateRTGSReport(ReportFilterDto reportFilter) throws BirtException {

        Map<String, Object> params = new HashMap<>();
        if (reportFilter != null) { 
            params.put("loginId", reportFilter.getLoginId());
            params.put("branchCd", reportFilter.getBranchCode());
            params.put("transferType", reportFilter.getTransactionType());
            params.put("Status", reportFilter.getStatus());
        }


        if (reportFilter.getAccountNo() != null && !reportFilter.getAccountNo().isEmpty()) {
            var detailsArray = reportFilter.getAccountNo().split("-");
            var accountNumberDto = new AccountNumberDto(detailsArray[0], detailsArray[1], detailsArray[2], detailsArray[3], detailsArray[4]);

            params.put("AccountBranchCd",accountNumberDto.getBranchCode());
            params.put("AccountTypeCd",accountNumberDto.getAccountType());
            params.put("customerNo",accountNumberDto.getCustomerNo());
            params.put("runNo",accountNumberDto.getRunNo());
            params.put("checkDigit",accountNumberDto.getCheckDigit());
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

        IReportRunnable report = engine.openReportDesign("/home/bahl/Project/outward-rtgs-da/src/main/resources/rtgs_report4.rptdesign");

        IRunAndRenderTask task = engine.createRunAndRenderTask(report);

        if (params!= null){
            task.setParameterValues(params);
        } 

        EXCELRenderOption options = new EXCELRenderOption();
        // PDFRenderOption options = new PDFRenderOption();
        options.setOutputFormat("pdf");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        options.setOutputStream(outputStream);

        task.setRenderOption(options);

        task.run();
        task.close();

       byte[] reportContent =  outputStream.toByteArray();
       return reportContent;
    }


    public byte[] generateVoucherReport(String voucherNo, String voucherYear) throws BirtException {

        Map<String, Object> params = new HashMap<>();
        params.put("VoucherNo", voucherNo);
        params.put("VoucherYear", voucherYear);


        EngineConfig engineConfig = new EngineConfig();
        engineConfig.setEngineHome("");
        Platform.startup(engineConfig);
        IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
        IReportEngine engine = factory.createReportEngine(engineConfig);

        IReportRunnable report = engine.openReportDesign("/home/bahl/Project/outward-rtgs-da/src/main/resources/vouch_report.rptdesign");

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
