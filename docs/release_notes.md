---
layout: default
title: Release Notes
---

Below are release notes and downloads for all of the Randomizer's versions, old and new.

## FVX

These release notes / downloads are mirrored from [UPR FVX's GitHub page](https://github.com/upr-fvx/universal-pokemon-randomizer-fvx/releases). 

<table class="versiontable">
	<tr><th>Version/link</th> <th>Release date</th> <th>Download</th></tr>
{% for rn in site.release_notes reversed %}
	{% if rn.url contains "old/" %} {% continue %} {% endif %}
	<tr>
		<td><a href="{{ site.baseurl }}{{ rn.url }}">{{ rn.name }}</a></td>
		<td>{{ rn.date | date: '%F' }}</td>
		<td><a href="{{ rn.download }}">Download</a></td>
	</tr>
{% endfor %}
</table>

## Closer-to-Vanilla

This release note / download is mirrored from [Closer-to-Vanilla's GitHub page](https://github.com/foxoftheasterisk/UPR-ZX-closer-to-vanilla/releases).

<table class="versiontable">
	<tr><th>Version/link</th> <th>Release date</th> <th>Download</th></tr>
{% for rn in site.release_notes reversed %}
	{% unless rn.url contains "old/CTV/" %} {% continue %} {% endunless %}
	<tr>
		<td><a href="{{ site.baseurl }}{{ rn.url }}">{{ rn.name }}</a></td>
		<td>{{ rn.date | date: '%F' }}</td>
		<td><a href="{{ rn.download }}">Download</a></td>
	</tr>
{% endfor %}
</table>

## V Branch

These release notes / downloads are mirrored from [the V branch's GitHub page](https://github.com/voliol/universal-pokemon-randomizer/releases). 

<table class="versiontable">
	<tr><th>Version/link</th> <th>Release date</th> <th>Download</th></tr>
{% for rn in site.release_notes reversed %}
	{% unless rn.url contains "old/V_branch/" %} {% continue %} {% endunless %}
	<tr>
		<td><a href="{{ site.baseurl }}{{ rn.url }}">{{ rn.name }}</a></td>
		<td>{{ rn.date | date: '%F' }}</td>
		<td><a href="{{ rn.download }}">Download</a></td>
	</tr>
{% endfor %}
</table>

## ZX

These release notes / downloads are mirrored from [UPR ZX's GitHub page](https://github.com/ajarmar/universal-pokemon-randomizer-zx/releases). 

<table class="versiontable">
	<tr><th>Version/link</th> <th>Release date</th> <th>Download</th></tr>
{% for rn in site.release_notes reversed %}
	{% unless rn.url contains "old/ZX/" %} {% continue %} {% endunless %}
	<tr>
		<td><a href="{{ site.baseurl }}{{ rn.url }}">{{ rn.name }}</a></td>
		<td>{{ rn.date | date: '%F' }}</td>
		<td><a href="{{ rn.download }}">Download</a></td>
	</tr>
{% endfor %}
</table>

## Original UPR

These release notes / downloads are mirrored from [an archived version of Dabomstew's original UPR website](https://web.archive.org/web/20231226230806/https://pokehacks.dabomstew.com/randomizer/olddownloads.php). 

<table class="versiontable">
	<tr><th>Version/link</th> <th>Release date</th> <th>Download</th></tr>
{% for rn in site.release_notes reversed %}
	{% unless rn.url contains "old/original/" %} {% continue %} {% endunless %}
	<tr>
		<td><a href="{{ site.baseurl }}{{ rn.url }}">{{ rn.name }}</a></td>
		<td>{{ rn.date | date: '%F' }}</td>
		<td>{% if rn.download %} <a href="{{ rn.download }}">Download </a>{% else %} --- {% endif %}</td>
	</tr>
{% endfor %}
</table>