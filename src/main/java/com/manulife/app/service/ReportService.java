package com.manulife.app.service;

import com.manulife.app.entity.User;
import com.manulife.app.repository.UserRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReportService {
    private String originFileName = "/manulife.jrxml";

    @Autowired
    private UserRepository userRepository;

    public byte[] generateReport(Long id) throws Exception {
        Optional<User> userOptional = this.userRepository.findById(id);
        if (userOptional.isPresent()) {
            JasperReport jasperReport = JasperCompileManager.compileReport(getClass().getResourceAsStream(originFileName));

            List<String> students = new ArrayList<>();
            students.add("abc");

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(students);

            User user = userOptional.get();

            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("name", user.getName());
            parameters.put("email", user.getEmail());

            JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            return JasperExportManager.exportReportToPdf(print);
        } else {
            throw new Exception("id not found");
        }
    }
}
