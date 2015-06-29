# Changes to be made (reference Sauce Account)

```
	export SAUCE_USERNAME=your_username
	export SAUCE_ACCESS_KEY=your_access_key
```

# Running the tests
to run: `mvn test`

# To fetch job information
```
	curl -u ndmanvar:c4b0109c-8bb4-4ed9-a162-b77ab58e2650 \
		https://saucelabs.com/rest/v1/ndmanvar/jobs/YOUR_JOB_ID
```

# How is it updating custom data (i.e. setting rally custom id)?
See setRallyIDCustomData in SampleSauceTest.java.

Using put REST API http request to set custom data. Can retrieve it at later point in time using REST API.
