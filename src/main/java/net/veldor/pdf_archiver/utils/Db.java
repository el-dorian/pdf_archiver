package net.veldor.pdf_archiver.utils;

import net.veldor.pdf_parser.model.selection.Conclusion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.HashMap;
import java.util.Locale;

public class Db {
    private ResultSet result;
    private Statement statement;

    private Db() throws Exception {
        // подключусь к базе данных
        // opening database connection to MySQL server
        // read db data from file
        File dbSettingsFile = new File("archive_db.conf");
        if (dbSettingsFile.isFile()) {
            try (BufferedReader br = new BufferedReader(new FileReader(dbSettingsFile))) {
                String line;
                int lineCount = 0;
                while ((line = br.readLine()) != null) {
                    if (lineCount == 0) {
                        // this is db address
                        url = line;
                    } else if (lineCount == 1) {
                        // this is db address
                        user = line;
                    } else if (lineCount == 2) {
                        // this is db address
                        password = line;
                    }
                    lineCount++;
                }
            }
        } else {
            boolean create = dbSettingsFile.createNewFile();
            System.out.println(create);
            throw new Exception("Нет настроек дб!");
        }
        mConnection = DriverManager.getConnection(url, user, password);
    }

    private static Db instance;

    public static Db getInstance() throws Exception {
        if (instance == null) {
            instance = new Db();
        }
        return instance;
    }

    private static final HashMap<String, Integer> diagnosticiansCache = new HashMap<>();
    private static final HashMap<String, Integer> areasCache = new HashMap<>();
    private static final HashMap<String, Integer> contrastsCache = new HashMap<>();
    private static String url;
    private static String user;
    private static String password;
    private final Connection mConnection;
    private String query;

    /**
     * Возвращает заготовку для запроса
     *
     * @return Statement
     */
    private Statement getStatement() throws SQLException {
        return mConnection.createStatement();
    }

    public void handleConclusion(Conclusion conclusion) throws Exception {
        try {
            // нужно занести данные в таблицу, предварительно убедившись, что их там нет
            if (conclusion != null) {
                // данные о пациенте
                int patientId = insertPatientData(conclusion);
                // данные о враче
                // сначала поищу в кеше, если там нет-запрошу значение в БД и положу в кеш
                int diagnosticianId;
                if (diagnosticiansCache.containsKey(conclusion.diagnostician)) {
                    diagnosticianId = diagnosticiansCache.get(conclusion.diagnostician);
                } else {
                    diagnosticianId = insertDiagnosticianData(conclusion);
                    diagnosticiansCache.put(conclusion.diagnostician, diagnosticianId);
                }

                int areaId;
                if (areasCache.containsKey(conclusion.executionArea)) {
                    areaId = areasCache.get(conclusion.executionArea);
                } else {
                    areaId = insertScannedAreaData(conclusion);
                    areasCache.put(conclusion.executionArea, areaId);
                }
                int contrastId;
                if (contrastsCache.containsKey(conclusion.contrastInfo)) {
                    contrastId = contrastsCache.get(conclusion.contrastInfo);
                } else {
                    contrastId = insertContrastData(conclusion);
                    contrastsCache.put(conclusion.contrastInfo, contrastId);
                }

                // check existent in db
                if (checkExistent(conclusion.executionNumber, conclusion.executionArea, conclusion.patientName) > 0) {
                    System.out.println("Файл уже в базе данных");
                    throw new ExistsInDbException("Файл уже в БД");
                }

                int textId = insertText(conclusion);
                // И, наконец, внесу данные о самом обследовании
                insertExecution(
                        patientId,
                        diagnosticianId,
                        areaId,
                        contrastId,
                        textId,
                        conclusion
                );
            }
        } catch (SQLException e) {
            System.out.println("can't insert data( " + e.getMessage());
            throw e;
        }
    }

    private void insertExecution(int patientId, int diagnosticianId, int areaId, int contrastId, int textId, Conclusion conclusion) throws SQLException {
        query = String.format(Locale.ENGLISH, "INSERT INTO `rdc_archive`.`execution` (`execution_number`, `execution_date`, `patient`, `doctor`, `execution_area`, `contrast`, `path`, `text`, `md5`) VALUES ('%s', '%s', '%d', '%d', '%d', '%d', '%s', '%d', '%s');",
                conclusion.executionNumber,
                conclusion.executionDate,
                patientId,
                diagnosticianId,
                areaId,
                contrastId,
                conclusion.filePath,
                textId,
                conclusion.hash);
        executeUpdateQuery(query);
    }

    private int insertContrastData(Conclusion conclusion) throws SQLException {
        query = String.format(Locale.ENGLISH, "CALL insertContrast('%s');", conclusion.contrastInfo);
        result = executeQuery(query);
        result.next();
        return result.getInt(1);
    }

    private int insertText(Conclusion conclusion) throws SQLException {
        query = String.format(Locale.ENGLISH, "CALL insertText('%s');", conclusion.conclusionText);
        result = executeQuery(query);
        result.next();
        return result.getInt(1);
    }

    private int insertScannedAreaData(Conclusion conclusion) throws SQLException {
        query = String.format(Locale.ENGLISH, "CALL insertExecutionArea('%s');", conclusion.executionArea);
        result = executeQuery(query);
        result.next();
        return result.getInt(1);
    }

    private int insertDiagnosticianData(Conclusion conclusion) throws SQLException {
        // если пациент ещё не зарегистрирован-зарегистрирую его
        query = String.format(Locale.ENGLISH, "CALL insertDiagnostician('%s');", conclusion.diagnostician);
        result = executeQuery(query);
        result.next();
        return result.getInt(1);
    }


    private int insertPatientData(Conclusion conclusion) throws SQLException {
        query = String.format(Locale.ENGLISH, "CALL insertPatient('%s', '%s', '%s');", conclusion.patientName, conclusion.patientBirthdate, conclusion.patientSex);
        System.out.println(query);
        result = executeQuery(query);
        result.next();
        return result.getInt(1);
    }

    private ResultSet executeQuery(String query) throws SQLException {
        statement = getStatement();
        return statement.executeQuery(query);
    }


    private void executeUpdateQuery(String query) throws SQLException {
        statement = getStatement();
        statement.executeUpdate(query);
    }

    public int checkExistent(String executionNumber, String executionArea, String patientName) throws Exception {
        Db db = Db.getInstance();
        String query = String.format(Locale.ENGLISH, "select count(*) from execution_info where execution_area = '%s' and execution_number = '%s' and patient_name = '%s';", executionArea, executionNumber, patientName);
        try (ResultSet result = db.executeQuery(query)) {
            result.next();
            return result.getInt(1);
        }
    }
}
