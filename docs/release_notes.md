---
layout: default
title: Release Notes
---

Below are the release notes for all of the Randomizer's versions, 
old and new.

## FVX



<table>
	<tr><th>Version/link</th> <th>Release date</th></tr>
{% for rn in site.release_notes reversed %}
	{% if rn.url contains "old/" %} {% continue %} {% endif %}
	<tr>
		<td><a href="{{ site.baseurl }}{{ rn.url }}">{{ rn.name }}</a></td>
		<td>{{ rn.date | date: '%F' }}</td>
	</tr>
{% endfor %}
</table>

## Closer-to-Vanilla

<table>
	<tr><th>Version/link</th> <th>Release date</th></tr>
{% for rn in site.release_notes reversed %}
	{% unless rn.url contains "old/CTV/" %} {% continue %} {% endunless %}
	<tr>
		<td><a href="{{ site.baseurl }}{{ rn.url }}">{{ rn.name }}</a></td>
		<td>{{ rn.date | date: '%F' }}</td>
	</tr>
{% endfor %}
</table>

## V Branch

<table>
	<tr><th>Version/link</th> <th>Release date</th></tr>
{% for rn in site.release_notes reversed %}
	{% unless rn.url contains "old/V_branch/" %} {% continue %} {% endunless %}
	<tr>
		<td><a href="{{ site.baseurl }}{{ rn.url }}">{{ rn.name }}</a></td>
		<td>{{ rn.date | date: '%F' }}</td>
	</tr>
{% endfor %}
</table>

## ZX

<table>
	<tr><th>Version/link</th> <th>Release date</th></tr>
{% for rn in site.release_notes reversed %}
	{% unless rn.url contains "old/ZX/" %} {% continue %} {% endunless %}
	<tr>
		<td><a href="{{ site.baseurl }}{{ rn.url }}">{{ rn.name }}</a></td>
		<td>{{ rn.date | date: '%F' }}</td>
	</tr>
{% endfor %}
</table>

## Original UPR

<table>
	<tr><th>Version/link</th> <th>Release date</th></tr>
{% for rn in site.release_notes reversed %}
	{% unless rn.url contains "old/original/" %} {% continue %} {% endunless %}
	<tr>
		<td><a href="{{ site.baseurl }}{{ rn.url }}">{{ rn.name }}</a></td>
		<td>{{ rn.date | date: '%F' }}</td>
	</tr>
{% endfor %}
</table>