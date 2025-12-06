---
layout: default
title: Release Notes
---

Below are the release notes for all of the Randomizer's versions, 
old and new.

## FVX

<table>
{% comment %} This big block identifies the previous and next releases {% endcomment %}
{% for rn in site.release_notes reversed %}

    {% assign version_prev = "" %}
    {% assign version_next = "" %}

    {% assign split_url = rn.url | split: "/" %} {% assign folder = split_url | pop %}
    {% assign version = split_url | last %}
    =={{ rn.url }}==<br>
    version={{ version }}<br>

    {% assign i_last = "" %}
    {% for rn2 in site.release_notes %}
        {% assign split_url = rn2.url | split: "/" %} {% assign folder2 = split_url | pop %}
        {% unless folder2 == folder %} {% continue %} {% endunless %}

        i_last={{ i_last }}<br>
        {% if i_last == version %} {% assign version_next = split_url | last %} ESCAPED!<br> {% break %} {% endif %}
        {% assign i_last = split_url | last %}
    {% endfor %}

    reversing...<br>
    {% assign i_last = "" %}
    {% for rn2 in site.release_notes reversed %}
        {% assign split_url = rn2.url | split: "/" %} {% assign folder2 = split_url | pop %}
        {% unless folder2 == folder %} {% continue %} {% endunless %}

        i_last={{ i_last }}<br>
        {% if i_last == version %} {% assign version_prev = split_url | last %} ESCAPED!<br> {% break %} {% endif %}
        {% assign i_last = split_url | last %}
    {% endfor %}

    prev: {{ version_prev }}<br>
    next: {{ version_next }}<br>
{% endfor %}
</table>

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