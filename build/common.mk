ifeq ($(CONTRIB), true)
GIT_DIR := $(BASE)/..
else
GIT_DIR := $(BASE)
endif

VERSION_XML := res/values/version.xml
GIT_LOG_HEAD := $(GIT_DIR)/.git/logs/HEAD

.IGNORE : $(GIT_LOG_HEAD)

.PHONY: android-studio lintClean

android-studio: prebuild

res/values/version.xml: $(GIT_LOG_HEAD) AndroidManifest.xml
	$(BASE)/scripts/createVersionXML.sh -c .

LINT_BINARY := $(ANDROID_HOME)/tools/lint

lint-results.html: lint.xml $(wildcard src/**/*) $(wildcard res/**/*)
	gradle lint
	cp --reflink=auto build/reports/$@ $@

lint.xml:
	ln -rs $(BASE)/build/lint.xml

lintClean:
	rm -f lint-results.html
