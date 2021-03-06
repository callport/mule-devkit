<?cs set:section = "mule" ?>
<?cs include:"doctype.cs" ?>
<?cs include:"macros.cs" ?>
<html>
<?cs include:"head_tag.cs" ?>
<body class="gc-documentation">
<?cs call:custom_masthead() ?>

<div class="g-unit" id="all-content">

<div id="jd-header">
<h1><?cs var:page.title ?></h1>
</div>

<div id="jd-content">

<div class="jd-descr">
<p><?cs call:tag_list(root.descr) ?></p>
</div>

<?cs set:count = #1 ?>
<table class="jd-sumtable">
<?cs each:pkg = docs.packages ?>
    <?cs each:mod = pkg.modules ?>
    <tr class="<?cs if:count % #2 ?>alt-color<?cs /if ?> api" >
        <td class="jd-linkcol"><?cs call:module_link(mod) ?></td>
        <td class="jd-descrcol"><?cs var:mod.namespace ?></td>
        <td class="jd-descrcol" width="100%"><?cs call:tag_list(mod.shortDescr) ?></td>
    </tr>
    <?cs /each ?>
<?cs set:count = count + #1 ?>
<?cs /each ?>
</table>

<?cs include:"footer.cs" ?>
</div><!-- end jd-content -->
</div> <!-- end doc-content -->

<?cs include:"trailer.cs" ?>

</body>
</html>
