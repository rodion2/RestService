package model.common.service.impl;

import model.common.exception.HandbookException;
import model.common.service.WinApiHandbookService;
import model.WinApiClass;
import model.WinApiFunction;
import model.WinApiParameter;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class WinApiHandbookServiceJdbc implements WinApiHandbookService {

    public static final String SELECT_PARAMETERS_BY_FUNCTION = "SELECT * FROM WINAPI_PARAMETER WHERE function_id = ?";
    public static final String SELECT_LIKE_NAME = "SELECT * FROM WINAPI_CLASS WHERE name LIKE ?";
    public static final String DELETE_PARAM = "DELETE FROM WINAPI_PARAMETER WHERE id=?";
    public static final String UPDATE_PARAM = "UPDATE WINAPI_PARAMETER SET NAME=?, TYPE=? WHERE ID=?";
    public static final String DELETE_FUNCTION = "DELETE FROM WINAPI_FUNCTION WHERE ID=?";
    public static final String UPDATE_CLASS = "UPDATE WINAPI_CLASS SET NAME=?, DESCRIPTION=? WHERE ID=?";
    public static final String UPDATE_FUNCTION = "UPDATE WINAPI_FUNCTION SET NAME=?, DESCRIPTION=? WHERE id=?";
    private static final String SELECT_BY_ID = "SELECT * FROM WINAPI_CLASS WHERE id=?";
    private static final String SELECT_BY_CLASS = "SELECT * FROM WINAPI_FUNCTION WHERE class_id=?";
    public static Logger log = Logger.getLogger(WinApiHandbookServiceJdbc.class);
    private final JdbcTemplate template;
    private RowMapper<WinApiClass> winApiClassRowMapper = (rs, rowNum) -> {
        WinApiClass clazz = new WinApiClass();
        clazz.setId(rs.getLong("id"));
        clazz.setName(rs.getString("name"));
        clazz.setDescription(rs.getString("description"));
        return clazz;
    };
    private RowMapper<WinApiFunction> winApiFunctionRowMapper = (rs, rowNum) -> {
        WinApiFunction function = new WinApiFunction();
        function.setId(rs.getLong("id"));
        function.setName(rs.getString("name"));
        function.setDescription(rs.getString("description"));
        return function;
    };
    private RowMapper<WinApiParameter> winApiParameterRowMapper = (rs, rowNum) -> {
        WinApiParameter parameter = new WinApiParameter();
        parameter.setId(rs.getLong("id"));
        parameter.setType(rs.getString("type"));
        parameter.setName(rs.getString("name"));
        return parameter;
    };

    public WinApiHandbookServiceJdbc(DataSource dataSource) {
            template = new JdbcTemplate(dataSource);
    }

    @Override
    public WinApiClass getWinApiClass(long id) throws HandbookException {
        WinApiClass winApiClass = this.template.queryForObject(SELECT_BY_ID, new Object[]{id}, winApiClassRowMapper);
        winApiClass.setFunctions(getFunctionByClass(winApiClass));
        return winApiClass;
    }

    @Override
    public List<WinApiClass> findClasses(String keyword) throws HandbookException {
        List<WinApiClass> query = template.query(SELECT_LIKE_NAME, new Object[]{"%" + keyword + "%"}, winApiClassRowMapper);
        return query.stream().map(c -> {
            List<WinApiFunction> functionByClass = getFunctionByClass(c);
            c.setFunctions(functionByClass);
            return c;
        }).collect(toList());
    }

    public List<WinApiFunction> getFunctionByClass(WinApiClass winApiClass) {
        List<WinApiFunction> query = template.query(SELECT_BY_CLASS, new Object[]{winApiClass.getId()}, winApiFunctionRowMapper);
        return query.stream().map(f -> {
            List<WinApiParameter> params = getParameterByFunction(f.getId());
            f.setParams(params);
            return f;
        }).collect(toList());
    }

    public List<WinApiParameter> getParameterByFunction(long functionId) {
        return template.query(SELECT_PARAMETERS_BY_FUNCTION, new Object[]{functionId}, winApiParameterRowMapper);
    }

    @Override
    public WinApiClass saveOrUpdate(WinApiClass winApiClass) throws HandbookException {
        log.debug(format("save or update class [id=%d]", winApiClass.getId()));
        long id = winApiClass.getId();
        if (winApiClass.getId() == 0) {
            id = createNewClass(winApiClass).getId();
        } else {
            updateClass(winApiClass);
        }
        return getWinApiClass(id);
    }

    @Override
    public void removeClass(long id) throws HandbookException {
        template.update("DELETE FROM WINAPI_CLASS WHERE ID=?", id);
    }

    @Override
    public void updateFunction(WinApiFunction function) throws HandbookException {
        template.update(UPDATE_FUNCTION,
                function.getName(), function.getDescription(), function.getId());
        for (WinApiParameter parameter : function.getParams()) {
            if (parameter.getId() == 0) {
                createParam(function.getId(), parameter);
            } else {
                updateParam(parameter);
            }
        }
    }

    @Override
    public void removeWinApiFunction(long id) throws HandbookException {
        template.update(DELETE_FUNCTION, id);
    }

    @Override
    public void updateParam(WinApiParameter parameter) throws HandbookException {
        template.update(UPDATE_PARAM,
                parameter.getName(), parameter.getType(), parameter.getId());
    }

    @Override
    public void removeWinApiParameter(long id) throws HandbookException {
        template.update(DELETE_PARAM, id);
    }

    private WinApiClass createNewClass(WinApiClass topic) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(template);

        MapSqlParameterSource params = mapClassToRow(topic);
        Number id = insert.usingGeneratedKeyColumns("id")
                .withTableName("WINAPI_CLASS")
                .executeAndReturnKey(params);
        topic.setId(id.longValue());
        topic.getFunctions().forEach(f -> {
            createFunction(topic.getId(), f);
        });

        return topic;
    }

    private void createFunction(long topicId, WinApiFunction f) {
        SimpleJdbcInsert functionInsert = new SimpleJdbcInsert(template)
                .usingGeneratedKeyColumns("id")
                .withTableName("WINAPI_FUNCTION");
        MapSqlParameterSource mapSqlParameterSource = mapFunctionToRow(topicId, f);
        Number number = functionInsert
                .executeAndReturnKey(mapSqlParameterSource);
        f.setId(number.longValue());
        f.getParams().forEach(p -> {
            createParam(f.getId(), p);
        });
    }

    private void createParam(long functionId, WinApiParameter p) {
        SimpleJdbcInsert paramsInsert = new SimpleJdbcInsert(template)
                .usingGeneratedKeyColumns("id")
                .withTableName("WINAPI_PARAMETER");
        MapSqlParameterSource parameterParams = mapParameterToRow(functionId, p);
        Number parameterId = paramsInsert
                .executeAndReturnKey(parameterParams);
        p.setId(parameterId.longValue());
    }

    private MapSqlParameterSource mapClassToRow(WinApiClass topic) {
        return new MapSqlParameterSource()
                .addValue("name", topic.getName())
                .addValue("description", topic.getDescription());
    }

    private MapSqlParameterSource mapFunctionToRow(long classId, WinApiFunction f) {
        return new MapSqlParameterSource()
                .addValue("name", f.getName())
                .addValue("description", f.getDescription())
                .addValue("class_id", classId);
    }

    private MapSqlParameterSource mapParameterToRow(long functionId, WinApiParameter p) {
        return new MapSqlParameterSource()
                .addValue("name", p.getName())
                .addValue("type", p.getType())
                .addValue("function_id", functionId);
    }

    public void updateClass(WinApiClass winApiClass) throws HandbookException {
        template.update(UPDATE_CLASS,
                winApiClass.getName(), winApiClass.getDescription(), winApiClass.getId());
        for (WinApiFunction function : winApiClass.getFunctions()) {
            if (function.getId() == 0) {
                createFunction(winApiClass.getId(), function);
            } else {
                updateFunction(function);
            }
        }
    }
}
