const { SlashCommandBuilder } = require('@discordjs/builders');

module.exports = {
	data: new SlashCommandBuilder()
		.setName('unlink')
		.setDescription('ADMIN: Unlink accounts.')
		.addStringOption(option =>
			option.setName('type')
				.setDescription('Type of account')
				.setRequired(true)
				.addChoices({
					name: 'ID', 
					value: 'id'
				}, {
					name: 'Discord Tag',
					value: 'discord'
				}))
		.addUserOption(option =>
			option.setName('tag')
				.setDescription('Discord Tag'))
		.addStringOption(option =>
			option.setName('id')
				.setDescription('Account id'))
};