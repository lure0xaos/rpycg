package gargoyle.rpycg.service;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import gargoyle.rpycg.ex.AppException;
import gargoyle.rpycg.fx.FXContext;
import gargoyle.rpycg.fx.FXLoad;
import gargoyle.rpycg.model.ModelItem;
import gargoyle.rpycg.model.Settings;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class CodeConverter {
    public static final int SPACES = 4;
    private static final String BOOLEAN_FORMAT = "True,False";
    private static final String GAME_VARIABLES = "Game Variables.txt";
    private static final String PARAM_ENABLE_CHEAT = "enableCheat";
    private static final String PARAM_ENABLE_CONSOLE = "enableConsole";
    private static final String PARAM_ENABLE_DEVELOPER = "enableDeveloper";
    private static final String PARAM_ENABLE_ROLLBACK = "enableRollback";
    private static final String PARAM_ENABLE_WRITE = "enableWrite";
    private static final String PARAM_FILE_VARIABLES = "fileVariables";
    private static final String PARAM_KEY_CHEAT = "keyCheat";
    private static final String PARAM_KEY_CONSOLE = "keyConsole";
    private static final String PARAM_KEY_DEVELOPER = "keyDeveloper";
    private static final String PARAM_KEY_WRITE = "keyWrite";
    private static final String PARAM_MODEL = "model";
    private static final String PARAM_MSG = "msg";
    private static final String PARAM_SETTINGS = "settings";
    private static final String TEMPLATE = "RenPyCheat.ftl";
    private static final Logger log = LoggerFactory.getLogger(CodeConverter.class);
    @NotNull
    private final Configuration configuration;
    @NotNull
    private final String fileVariables;
    @NotNull
    private final KeyConverter keyConverter;
    @NotNull
    private final ResourceBundleMethodModel resourceBundleMethodModel;
    @NotNull
    private final Settings settings;

    public CodeConverter(@NotNull FXContext context, @NotNull Settings settings, int spaces) {
        this.settings = settings;
        keyConverter = new KeyConverter();
        fileVariables = GAME_VARIABLES;
        configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        configuration.setClassLoaderForTemplateLoading(getClass().getClassLoader(),
                getClass().getPackage().getName().replace('.', '/'));
        configuration.setEncoding(context.getLocale(), context.getCharset().name());
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(true);
        configuration.setWrapUncheckedExceptions(true);
        configuration.setFallbackOnNullLoopVariable(false);
        configuration.setWhitespaceStripping(false);
        configuration.setBooleanFormat(BOOLEAN_FORMAT);
        configuration.setSharedVariable(IndentDirective.DIRECTIVE_NAME, new IndentDirective(spaces));
        resourceBundleMethodModel = new ResourceBundleMethodModel(context, getClass());
    }

    @SuppressWarnings("OverlyBroadCatchBlock")
    @NotNull
    public List<String> toCode(@NotNull ModelItem menu) {
        try (StringWriter writer = new StringWriter()) {
            configuration.getTemplate(TEMPLATE).process(Map.of(
                    PARAM_FILE_VARIABLES, fileVariables,
                    PARAM_SETTINGS, Map.of(
                            PARAM_ENABLE_CHEAT, settings.getEnableCheat(),
                            PARAM_ENABLE_CONSOLE, settings.getEnableConsole(),
                            PARAM_ENABLE_DEVELOPER, settings.getEnableDeveloper(),
                            PARAM_ENABLE_WRITE, settings.getEnableWrite(),
                            PARAM_ENABLE_ROLLBACK, settings.getEnableRollback(),
                            PARAM_KEY_CHEAT, keyConverter.toBinding(settings.getKeyCheat()),
                            PARAM_KEY_CONSOLE, keyConverter.toBinding(settings.getKeyConsole()),
                            PARAM_KEY_DEVELOPER, keyConverter.toBinding(settings.getKeyDeveloper()),
                            PARAM_KEY_WRITE, keyConverter.toBinding(settings.getKeyWrite())),
                    PARAM_MSG, resourceBundleMethodModel,
                    PARAM_MODEL, menu), writer);
            return Arrays.asList(writer.toString().split(System.lineSeparator()));
        } catch (TemplateException | MissingResourceException | IOException e) {
            throw new IllegalStateException(e.getLocalizedMessage(), e);
        }
    }

    private static final class IndentDirective implements TemplateDirectiveModel {
        private static final String DIRECTIVE_NAME = "indent";
        private static final String PARAM_NAME = "count";
        private final int spaces;

        private IndentDirective(int spaces) {
            this.spaces = spaces;
        }

        @Override
        public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
                throws TemplateException, IOException {
            Object value = params.get(PARAM_NAME);
            if (!(value instanceof TemplateNumberModel)) {
                throw new TemplateModelException(String.format("The \"%s\" parameter must be a number", PARAM_NAME));
            }
            int count = ((TemplateNumberModel) value).getAsNumber().intValue();
            if (count < 0) {
                throw new TemplateModelException(String.format("The \"%s\" parameter cannot be negative", PARAM_NAME));
            }
            String message;
            try (StringWriter writer = new StringWriter()) {
                body.render(writer);
                message = writer.toString();
            }
            Writer out = env.getOut();
            for (String token : message.split("\n")) {
                out.write(" ".repeat(spaces * count));
                out.write(token);
                out.write(message.contains("\n") ? "\n" : "");
            }
        }
    }

    private static final class ResourceBundleMethodModel implements TemplateMethodModelEx {
        private final Class<?> aClass;
        private final FXContext context;
        private final ResourceBundle resources;

        private ResourceBundleMethodModel(@NotNull FXContext context, Class<?> aClass) {
            this.aClass = aClass;
            this.context = context;
            resources = FXLoad.loadResources(context, FXLoad.getBaseName(aClass))
                    .orElseThrow(() -> new AppException("resources not found for " + aClass));
        }

        @Override
        public Object exec(List arguments) throws TemplateModelException {
            if (arguments.isEmpty()) {
                throw new TemplateModelException("Wrong number of arguments");
            }
            String key;
            Object argument = arguments.get(0);
            if (argument instanceof SimpleScalar) {
                key = ((SimpleScalar) argument).getAsString();
            } else {
                key = String.valueOf(argument);
            }
            if (key == null || key.isBlank()) {
                throw new TemplateModelException(String.format("Invalid code value '%s' (%s)", key,
                        context.getLocale()));
            }
            if (resources.containsKey(key)) {
                return MessageFormat.format(resources.getString(key).trim(),
                        arguments.subList(1, arguments.size()).toArray());
            }
            log.warn("no key {} in resources for {} found", key, aClass);
            return key;
        }
    }
}

