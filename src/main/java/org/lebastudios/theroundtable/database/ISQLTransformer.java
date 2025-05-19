package org.lebastudios.theroundtable.database;

import org.lebastudios.theroundtable.plugins.IPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ISQLTransformer
{
    ISQLTransformer[] TRANSFORMERS = new ISQLTransformer[]{
            new ConditionalDBMSTransformer(),
            new AutoincrementTransformer(),
            new TableMigratorTransformer(),
    };

    List<String> transform(String sql, Dbms dbms, IPlugin plugin);

    default String replaceFirstGroup(String regex, String replacement, String text)
    {
        StringBuilder modifiedSql = new StringBuilder();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        int lastMatchEnd = 0;
        while (matcher.find())
        {
            modifiedSql.append(text, lastMatchEnd, matcher.start(1));
            modifiedSql.append(replacement);
            lastMatchEnd = matcher.end(1);
        }

        modifiedSql.append(text, lastMatchEnd, text.length());
        return modifiedSql.toString();
    }

    class AutoincrementTransformer implements ISQLTransformer
    {
        @Override
        public List<String> transform(String sql, Dbms dbms, IPlugin plugin)
        {
            if (sql.contains("CREATE TABLE") || sql.contains("create table"))
            {
                String replacement = switch (dbms)
                {
                    case SQLITE -> "autoincrement";
                    case MARIADB -> "auto_increment";
                };

                sql = this.replaceFirstGroup(" ((?i)AUTOINCREMENT)[ ,;]", replacement, sql);
            }

            return List.of(sql);
        }
    }

    class TableMigratorTransformer implements ISQLTransformer
    {
        private final static String regex =
                "[ \n]*(?:(?i)create table) (\\w*)[^(]*\\([ \n]*((?:\\w* [^,].*\n)+)[ \n]*-- NEW";
        private static final Pattern pattern = Pattern.compile(regex);


        @Override
        public List<String> transform(String sql, Dbms dbms, IPlugin plugin)
        {
            List<String> sqlList = new ArrayList<>();

            if (sql.trim().startsWith("-- MIGRATE"))
            {
                Matcher matcher = pattern.matcher(sql);

                while (matcher.find())
                {
                    String tableName = matcher.group(1);
                    String oldColDef = matcher.group(2);

                    String tmpTableName = tableName + "_tmp";
                    String deletedTableName = tableName + "_old";
                    List<String> colsNames = extractColsNames(oldColDef);

                    // Creating the tmp table
                    sqlList.add(this.replaceFirstGroup(regex, tmpTableName, sql));

                    // Copy data if exists
                    sqlList.add("insert into " + tmpTableName + " (" + String.join(",", colsNames) +
                            ") " + "select " + String.join(",", colsNames) + " from " + tableName + ";");

                    // dropping the old table and renaming the new one
                    sqlList.add("drop table if exists " + tableName + ";");
                    sqlList.add("alter table " + tmpTableName + " rename to " + tableName + ";");
                }
            }
            else
            {
                sqlList.add(sql);
            }

            return sqlList;
        }

        private List<String> extractColsNames(String createTableBody)
        {
            List<String> colsNames = new ArrayList<>();

            for (String line : createTableBody.split(", *\\R"))
            {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty()) continue;

                colsNames.add(trimmedLine.substring(0, trimmedLine.indexOf(" ")));
            }

            return colsNames;
        }
    }

    class ConditionalDBMSTransformer implements ISQLTransformer
    {
        @Override
        public List<String> transform(String sql, Dbms dbms, IPlugin plugin)
        {
            Pattern pattern = Pattern.compile("(?si)-- IF " + dbms.name() + " *\\R(.*?)-- ENDIF");
            List<String> sqlList = new ArrayList<>();

            Matcher matcher = pattern.matcher(sql);
            if (matcher.find())
            {
                String conditionBody = matcher.group(1);
                sqlList.add(conditionBody);
            }
            else
            {
                sqlList.add(sql);
            }

            return sqlList;
        }
    }
}
