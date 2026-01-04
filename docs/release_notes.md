---
layout: default
title: Release Notes
---

Below are the release notes for all of the Randomizer's versions, 
old and new.

## FVX



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