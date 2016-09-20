package org.codelibs.riverweb.transformer;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.codelibs.core.io.ResourceUtil;
import org.codelibs.fess.crawler.entity.ResponseData;
import org.codelibs.fess.crawler.entity.ResultData;
import org.codelibs.riverweb.config.RiverConfig;
import org.codelibs.riverweb.config.RiverConfigManager;
import org.junit.Test;

public class ScrapingTransformerTest {
    @Test
    public void fess_codelibs_org() {
        final RiverConfigManager riverConfigManager = new RiverConfigManager();
        final ScrapingTransformer transformer = new ScrapingTransformer() {
            @SuppressWarnings("unchecked")
            @Override
            protected void storeIndex(final ResponseData responseData, final Map<String, Object> dataMap) {
                System.out.println(dataMap);
                assertThat(((List<String>) ((Map<String, Object>) dataMap.get("nav")).get("sideMenus")).size(), is(27));
                assertThat(((Map<String, Object>) dataMap.get("section1")).get("title").toString(), is("What is Fess?"));
                assertThat(((List<String>) ((Map<String, Object>) dataMap.get("section1")).get("body")).size(), is(2));
                assertThat(((Map<String, Object>) dataMap.get("section2")).get("title").toString(), is("Features"));
                assertThat(((List<String>) ((Map<String, Object>) dataMap.get("section2")).get("body")).size(), is(12));
            }
        };
        transformer.riverConfigManager = riverConfigManager;

        final String sessionId = "test";
        final String url = "http://fess.codelibs.org/";
        final RiverConfig riverConfig = riverConfigManager.get(sessionId);
        transformer.riverConfigLocal.set(riverConfig);

        final Map<String, Map<String, Object>> scrapingRuleMap = new HashMap<String, Map<String, Object>>();
        addScrapingRuleMap(scrapingRuleMap, "text", "nav.sideMenus", "div.sidebar-nav ul li", Boolean.TRUE, Boolean.TRUE);
        addScrapingRuleMap(scrapingRuleMap, "text", "section1.title", "div.section:eq(0) h2", null, null);
        addScrapingRuleMap(scrapingRuleMap, "text", "section1.body", "div.section:eq(0) p", Boolean.TRUE, Boolean.TRUE);
        addScrapingRuleMap(scrapingRuleMap, "text", "section2.title", "div.section:eq(1) h2", null, null);
        addScrapingRuleMap(scrapingRuleMap, "text", "section2.body", "div.section:eq(1) ul li", Boolean.TRUE, Boolean.TRUE);
        final Map<String, Object> patternMap = new HashMap<String, Object>();
        patternMap.put("url", url);
        riverConfig.addScrapingRule(null, patternMap, scrapingRuleMap);
        InputStream is = null;
        try {
            final ResponseData responseData = new ResponseData();
            responseData.setSessionId(sessionId);
            responseData.setUrl(url);
            responseData.setResponseBody(ResourceUtil.getResourceAsFile("html/fess_codelibs_org.html"), false);
            responseData.setCharSet("UTF-8");
            final ResultData resultData = new ResultData();

            transformer.storeData(responseData, resultData);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private void addScrapingRuleMap(final Map<String, Map<String, Object>> scrapingRuleMap, final String type, final String property,
            final String path, final Boolean isArray, final Boolean trimSpaces) {
        final Map<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put(type, path);
        if (isArray != null) {
            valueMap.put("is_array", isArray);
        }
        if (trimSpaces != null) {
            valueMap.put("trim_spaces", trimSpaces);
        }
        scrapingRuleMap.put(property, valueMap);
    }
}
